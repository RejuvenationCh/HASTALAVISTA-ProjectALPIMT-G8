package dev.outfix.comfyui;

import dev.outfix.dto.GenerateMockupRequest;
import dev.outfix.dto.GenerateMockupResponse;
import dev.outfix.entity.User;
import dev.outfix.entity.Wardrobe;
import dev.outfix.repository.UserRepository;
import dev.outfix.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/mockup")
@RequiredArgsConstructor
public class ComfyUiController {

    private final ComfyUiService comfyUiService;
    private final UserRepository userRepository;
    private final WardrobeRepository wardrobeRepository;

    /**
     * POST /api/mockup/generate
     *
     * Triggers a ComfyUI render for the authenticated user's face model
     * paired with the selected wardrobe item. Returns the final image URL.
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateMockupResponse> generate(
            @RequestBody GenerateMockupRequest request,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        if (user.getFaceModelUrl() == null) {
            return ResponseEntity.badRequest()
                .body(GenerateMockupResponse.error("No face model uploaded. Please upload your face model first."));
        }

        Wardrobe item = wardrobeRepository.findById(request.getWardrobeItemId())
            .orElseThrow(() -> new IllegalArgumentException("Wardrobe item not found."));

        if (!item.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                .body(GenerateMockupResponse.error("Access denied."));
        }

        if (item.getClothingImageUrl() == null) {
            return ResponseEntity.badRequest()
                .body(GenerateMockupResponse.error("Selected wardrobe item has no image."));
        }

        try {
            // Extract just the filename — ComfyUI expects filenames relative to its input folder.
            String faceFilename = toFilename(user.getFaceModelUrl());
            String garmentFilename = toFilename(item.getClothingImageUrl());

            String outputFilename = comfyUiService.generateMockup(faceFilename, garmentFilename);

            if (outputFilename == null) {
                return ResponseEntity.ok(GenerateMockupResponse.timeout());
            }

            String viewUrl = comfyUiService.buildViewUrl(outputFilename);
            return ResponseEntity.ok(GenerateMockupResponse.success(viewUrl));

        } catch (Exception e) {
            log.error("ComfyUI generation failed for user={}, wardrobeItem={}", user.getUsername(), request.getWardrobeItemId(), e);
            return ResponseEntity.internalServerError()
                .body(GenerateMockupResponse.error("Generation failed: " + e.getMessage()));
        }
    }

    private String toFilename(String url) {
        Path path = Paths.get(url);
        return path.getFileName().toString();
    }

}
