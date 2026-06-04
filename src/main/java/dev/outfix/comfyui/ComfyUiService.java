package dev.outfix.comfyui;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyUiService {

    private final ComfyUiProperties props;
    private final ObjectMapper objectMapper;
    private final WebClient comfyUiWebClient;

    private final Random random = new Random();

    /**
     * Main entry point called by the controller.
     * Loads the CatVTON workflow, injects the two image filenames and clothing type,
     * submits it to ComfyUI, then polls until the output image is ready.
     *
     * clothingType: "TOP", "BOTTOM", or "DRESS"
     */
    public String generateMockup(String faceFilename, String garmentFilename, String clothingType)
            throws IOException, InterruptedException {

        ObjectNode workflow = buildWorkflow(faceFilename, garmentFilename, clothingType);
        String promptId = submitPrompt(workflow);
        log.info("Submitted to ComfyUI. prompt_id={}", promptId);
        return pollForOutputImage(promptId);
    }

    // --- Step 1: Build the workflow from the JSON template ---

    private ObjectNode buildWorkflow(String faceFilename, String garmentFilename, String clothingType)
            throws IOException {

        ClassPathResource resource = new ClassPathResource("comfyui/workflow_template.json");
        ObjectNode workflow = (ObjectNode) objectMapper.readTree(resource.getInputStream());

        // Inject person and garment image filenames into their LoadImage nodes
        setImageFilename(workflow, props.getNode().getFaceInputId(), faceFilename);
        setImageFilename(workflow, props.getNode().getGarmentInputId(), garmentFilename);

        // Randomize seed on node 305 (CatVTONWrapper) so each render is unique
        ObjectNode node305 = (ObjectNode) workflow.get("305").get("inputs");
        node305.put("seed", random.nextLong(1_000_000_000L));

        // Set clothing segmentation flags on node 307 based on clothingType
        ObjectNode node307 = (ObjectNode) workflow.get("307").get("inputs");
        applyClothingType(node307, clothingType);

        return workflow;
    }

    private void setImageFilename(ObjectNode workflow, String nodeId, String filename) {
        JsonNode node = workflow.get(nodeId);
        if (node == null) {
            throw new IllegalStateException(
                "Workflow template missing node id='" + nodeId + "'. " +
                "Check comfyui.node.face-input-id / garment-input-id in application.properties."
            );
        }
        ((ObjectNode) node.get("inputs")).put("image", filename);
    }

    /**
     * Turns on the correct boolean flags in the ClothesSegment node
     * based on what type of clothing the user selected.
     */
    private void applyClothingType(ObjectNode node307inputs, String clothingType) {
        // Reset all to false first
        node307inputs.put("Upper-clothes", false);
        node307inputs.put("Dress", false);
        node307inputs.put("Pants", false);
        node307inputs.put("Skirt", false);
        node307inputs.put("Scarf", false);

        switch (clothingType.toUpperCase()) {
            case "TOP"    -> node307inputs.put("Upper-clothes", true);
            case "BOTTOM" -> {
                node307inputs.put("Pants", true);
                node307inputs.put("Skirt", true);
            }
            case "DRESS"  -> node307inputs.put("Dress", true);
            default       -> node307inputs.put("Upper-clothes", true); // fallback
        }
    }

    // --- Step 2: POST the workflow to ComfyUI ---

    private String submitPrompt(ObjectNode workflow) {
        Map<String, Object> payload = Map.of(
            "prompt", workflow,
            "client_id", props.getClientId()
        );

        JsonNode response = comfyUiWebClient.post()
            .uri("/prompt")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (response == null || !response.has("prompt_id")) {
            throw new IllegalStateException("ComfyUI did not return a prompt_id. Response: " + response);
        }

        return response.get("prompt_id").asText();
    }

    // --- Step 3: Poll /history until the job is done ---

    private String pollForOutputImage(String promptId) throws InterruptedException {
        int maxAttempts = props.getPolling().getMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Thread.sleep(2000);

            JsonNode history = comfyUiWebClient.get()
                .uri("/history/{promptId}", promptId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (history == null || !history.has(promptId)) {
                log.debug("Poll {}/{}: not ready yet.", attempt, maxAttempts);
                continue;
            }

            JsonNode job = history.get(promptId);
            if (job.path("status").path("completed").asBoolean(false)) {
                String filename = extractOutputFilename(job);
                log.info("Render complete. output={}", filename);
                return filename;
            }

            log.debug("Poll {}/{}: status={}", attempt, maxAttempts,
                job.path("status").path("status_str").asText("running"));
        }

        log.warn("ComfyUI timed out after {} attempts for prompt_id={}", maxAttempts, promptId);
        return null;
    }

    private String extractOutputFilename(JsonNode job) {
        String outputNodeId = props.getNode().getOutputNodeId();
        JsonNode images = job.path("outputs").path(outputNodeId).path("images");

        if (images.isArray() && !images.isEmpty()) {
            return images.get(0).path("filename").asText();
        }

        throw new IllegalStateException(
            "Render finished but no image found at outputs[" + outputNodeId + "]. " +
            "Check comfyui.node.output-node-id in application.properties."
        );
    }

    public String buildViewUrl(String filename) {
        return props.getBaseUrl() + "/view?filename=" + filename + "&type=output";
    }
}
