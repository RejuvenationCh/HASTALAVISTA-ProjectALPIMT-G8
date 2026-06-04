package dev.outfix.comfyui;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import dev.outfix.dto.GenerateMockupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles HTTP requests for AI outfit mockup generation via ComfyUI.
 * All endpoints require a valid JWT token.
 *
 * Typical client flow:
 *   1. Upload images via POST /api/mockup/upload (once per image).
 *   2. Use the returned filename to trigger generation via /test or /full.
 *   3. The response contains an imageUrl pointing directly to the ComfyUI output.
 */
@Slf4j
@RestController
@RequestMapping("/api/mockup")
@RequiredArgsConstructor
public class ComfyUiController {

    private final ComfyUiService comfyUiService;
    private final WebClient comfyUiWebClient;

    /**
     * POST /api/mockup/upload
     * Forwards an image file to ComfyUI's input folder.
     * Must be called before /test or /full — ComfyUI needs filenames, not raw bytes.
     *
     * Send as multipart/form-data with a field named "image".
     * Returns: { "name": "uploaded_filename.png" }
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImageToComfyUi(
            @RequestParam("image") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file provided."));
        }

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("image", file.getResource());
            bodyBuilder.part("overwrite", "true");

            Map<?, ?> comfyResponse = comfyUiWebClient.post()
                    .uri("/upload/image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String uploadedFilename = comfyResponse != null
                    ? (String) comfyResponse.get("name") : null;

            if (uploadedFilename == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "ComfyUI did not return a filename."));
            }

            return ResponseEntity.ok(Map.of("name", uploadedFilename));

        } catch (Exception e) {
            log.error("Image upload to ComfyUI failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/mockup/test
     * Single-garment virtual try-on. Replaces one clothing item on the face photo.
     *
     * Request body:
     * {
     *   "faceFilename":    "face.png",
     *   "garmentFilename": "shirt.png",
     *   "clothingType":    "TOP"        (TOP | BOTTOM | DRESS | SHOES)
     * }
     */
    @PostMapping("/test")
    public ResponseEntity<GenerateMockupResponse> generateSingleGarment(
            @RequestBody Map<String, String> body) {

        String faceFilename    = body.get("faceFilename");
        String garmentFilename = body.get("garmentFilename");
        String clothingType    = body.getOrDefault("clothingType", "TOP");

        if (faceFilename == null || garmentFilename == null) {
            return ResponseEntity.badRequest()
                    .body(GenerateMockupResponse.error(
                            "faceFilename and garmentFilename are required."));
        }

        try {
            String outputFilename = comfyUiService.generateMockup(
                    faceFilename, garmentFilename, clothingType);

            if (outputFilename == null) {
                return ResponseEntity.ok(GenerateMockupResponse.timeout());
            }

            return ResponseEntity.ok(
                    GenerateMockupResponse.success(
                            comfyUiService.buildViewUrl(outputFilename)));

        } catch (Exception e) {
            log.error("Single-garment generation failed", e);
            return ResponseEntity.internalServerError()
                    .body(GenerateMockupResponse.error(
                            "Generation failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/mockup/full
     * Full outfit virtual try-on. Applies top, bottom, and shoes in three passes.
     *
     * Request body:
     * {
     *   "faceFilename":   "face.png",
     *   "topFilename":    "shirt.png",
     *   "bottomFilename": "pants.png",
     *   "shoesFilename":  "shoes.png"
     * }
     */
    @PostMapping("/full")
    public ResponseEntity<GenerateMockupResponse> generateFullOutfit(
            @RequestBody Map<String, String> body) {

        String faceFilename   = body.get("faceFilename");
        String topFilename    = body.get("topFilename");
        String bottomFilename = body.get("bottomFilename");
        String shoesFilename  = body.get("shoesFilename");

        boolean anyFilenameIsMissing = faceFilename == null || topFilename == null
                || bottomFilename == null || shoesFilename == null;

        if (anyFilenameIsMissing) {
            return ResponseEntity.badRequest()
                    .body(GenerateMockupResponse.error(
                            "faceFilename, topFilename, bottomFilename, "
                            + "and shoesFilename are all required."));
        }

        try {
            String[] result = comfyUiService.generateFullOutfit(
                    faceFilename, topFilename, bottomFilename, shoesFilename);

            // result[0] = JPG filename, result[1] = PNG filename
            if (result[0] == null) {
                return ResponseEntity.ok(GenerateMockupResponse.timeout());
            }

            String jpgUrl = comfyUiService.buildViewUrl(result[0]);
            String pngUrl = result[1] != null ? comfyUiService.buildViewUrl(result[1]) : null;
            return ResponseEntity.ok(GenerateMockupResponse.successWithBoth(jpgUrl, pngUrl));

        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError()
                    .body(GenerateMockupResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Full-outfit generation failed", e);
            return ResponseEntity.internalServerError()
                    .body(GenerateMockupResponse.error(
                            "Generation failed: " + e.getMessage()));
        }
    }
}
