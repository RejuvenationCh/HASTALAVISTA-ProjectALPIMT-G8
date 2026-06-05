package dev.outfix.wardrobe.service;

import java.nio.file.Paths;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import dev.outfix.comfyui.ComfyUiService;
import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.entity.WardrobeStatus;
import dev.outfix.wardrobe.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Separate component so @Async is invoked through a Spring proxy.
 * WardrobeService calls this after saving the wardrobe with PENDING status.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WardrobeMockupGenerator {

    private final WardrobeRepository wardrobeRepository;
    private final ComfyUiService comfyUiService;

    @Async
    public void generate(Long wardrobeId, User owner) {
        Wardrobe wardrobe = wardrobeRepository.findById(wardrobeId).orElseThrow();
        try {
            String faceComfyName   = upload(owner.getFaceModelUrl());
            String topComfyName    = upload(wardrobe.getTopClothing().getClothingImageUrl());
            String bottomComfyName = upload(wardrobe.getBottomClothing().getClothingImageUrl());

            // Top + pants only — the shoes pass is bypassed inside generateTopBottom.
            String jpgFilename = comfyUiService.generateTopBottom(
                    faceComfyName, topComfyName, bottomComfyName);

            if (jpgFilename != null) {
                wardrobe.setMockupJpgUrl(comfyUiService.buildViewUrl(jpgFilename));
                wardrobe.setMockupPngUrl(null);
                wardrobe.setStatus(WardrobeStatus.DONE);
            } else {
                // Render timed out — treat as a server error the user can retry.
                wardrobe.setStatus(WardrobeStatus.FAILED);
            }

        } catch (Exception e) {
            log.error("ComfyUI generation failed for wardrobe id={}", wardrobeId, e);
            wardrobe.setStatus(WardrobeStatus.FAILED);
        }
        wardrobeRepository.save(wardrobe);
    }

    private String upload(String localUrl) throws Exception {
        String path = localUrl.startsWith("/") ? localUrl.substring(1) : localUrl;
        return comfyUiService.uploadLocalFile(Paths.get(path));
    }
}
