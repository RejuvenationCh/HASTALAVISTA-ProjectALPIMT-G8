package dev.outfix.comfyui;

import dev.outfix.dto.GenerateMockupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mockup")
@RequiredArgsConstructor
public class ComfyUiController {

    private final ComfyUiService comfyUiService;
    private final WebClient comfyUiWebClient;

    /**
     * POST /api/mockup/upload
     * Proxies a multipart image upload to ComfyUI's /upload/image endpoint.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided."));
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("image", file.getResource());
            builder.part("overwrite", "true");

            Map<?, ?> comfyResponse = comfyUiWebClient.post()
                .uri("/upload/image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            String filename = comfyResponse != null ? (String) comfyResponse.get("name") : null;
            if (filename == null) {
                return ResponseEntity.internalServerError().body(Map.of("error", "ComfyUI did not return a filename."));
            }

            return ResponseEntity.ok(Map.of("name", filename));

        } catch (Exception e) {
            log.error("Upload to ComfyUI failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/mockup/test
     * Single garment try-on.
     * Body: { "faceFilename", "garmentFilename", "clothingType": TOP|BOTTOM|DRESS }
     */
    @PostMapping("/test")
    public ResponseEntity<GenerateMockupResponse> test(@RequestBody Map<String, String> body) {
        String faceFilename    = body.get("faceFilename");
        String garmentFilename = body.get("garmentFilename");
        String clothingType    = body.getOrDefault("clothingType", "TOP");

        if (faceFilename == null || garmentFilename == null) {
            return ResponseEntity.badRequest()
                .body(GenerateMockupResponse.error("faceFilename and garmentFilename are required."));
        }

        try {
            String outputFilename = comfyUiService.generateMockup(faceFilename, garmentFilename, clothingType);
            if (outputFilename == null) return ResponseEntity.ok(GenerateMockupResponse.timeout());
            return ResponseEntity.ok(GenerateMockupResponse.success(comfyUiService.buildViewUrl(outputFilename)));
        } catch (Exception e) {
            log.error("Single-garment generation failed", e);
            return ResponseEntity.internalServerError()
                .body(GenerateMockupResponse.error("Generation failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/mockup/full
     * Full outfit try-on (top + bottom + shoes, 3 sequential CatVTON passes).
     * Body: { "faceFilename", "topFilename", "bottomFilename", "shoesFilename" }
     */
    @PostMapping("/full")
    public ResponseEntity<GenerateMockupResponse> full(@RequestBody Map<String, String> body) {
        String faceFilename   = body.get("faceFilename");
        String topFilename    = body.get("topFilename");
        String bottomFilename = body.get("bottomFilename");
        String shoesFilename  = body.get("shoesFilename");

        if (faceFilename == null || topFilename == null || bottomFilename == null || shoesFilename == null) {
            return ResponseEntity.badRequest()
                .body(GenerateMockupResponse.error("faceFilename, topFilename, bottomFilename, and shoesFilename are all required."));
        }

        try {
            String outputFilename = comfyUiService.generateFullOutfit(
                faceFilename, topFilename, bottomFilename, shoesFilename);
            if (outputFilename == null) return ResponseEntity.ok(GenerateMockupResponse.timeout());
            return ResponseEntity.ok(GenerateMockupResponse.success(comfyUiService.buildViewUrl(outputFilename)));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError()
                .body(GenerateMockupResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Full-outfit generation failed", e);
            return ResponseEntity.internalServerError()
                .body(GenerateMockupResponse.error("Generation failed: " + e.getMessage()));
        }
    }
}
