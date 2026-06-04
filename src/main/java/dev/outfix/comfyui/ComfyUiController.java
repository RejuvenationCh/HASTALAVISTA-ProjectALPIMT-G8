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
     * The frontend calls this instead of hitting ComfyUI directly (avoids CORS).
     * Returns: { "name": "filename.jpg" }
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
     * Accepts filenames already uploaded to ComfyUI, runs the CatVTON pipeline,
     * and returns the output image URL.
     *
     * Body: { "faceFilename": "...", "garmentFilename": "...", "clothingType": "TOP|BOTTOM|DRESS" }
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
            log.error("Test generation failed", e);
            return ResponseEntity.internalServerError()
                .body(GenerateMockupResponse.error("Generation failed: " + e.getMessage()));
        }
    }
}
