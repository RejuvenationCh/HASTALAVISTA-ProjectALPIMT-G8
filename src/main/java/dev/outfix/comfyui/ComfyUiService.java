package dev.outfix.comfyui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyUiService {

    private final ComfyUiProperties props;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * Full pipeline: injects image paths into the workflow template,
     * submits to ComfyUI, then polls until the output image is ready.
     *
     * @param faceImageFilename     filename in ComfyUI's input folder (e.g. "face_123.png")
     * @param garmentImageFilename  filename in ComfyUI's input folder (e.g. "garment_456.png")
     * @return output image filename from ComfyUI's output folder, or null on timeout
     */
    public String generateMockup(String faceImageFilename, String garmentImageFilename) throws IOException, InterruptedException {
        ObjectNode workflow = buildWorkflow(faceImageFilename, garmentImageFilename);
        String promptId = submitPrompt(workflow);
        log.info("ComfyUI prompt submitted. prompt_id={}", promptId);
        return pollForOutputImage(promptId);
    }

    // -------------------------------------------------------------------------
    // Step 1: Load the JSON template and inject the two image filenames
    // -------------------------------------------------------------------------

    private ObjectNode buildWorkflow(String faceImageFilename, String garmentImageFilename) throws IOException {
        ClassPathResource resource = new ClassPathResource("comfyui/workflow_template.json");
        ObjectNode template = (ObjectNode) objectMapper.readTree(resource.getInputStream());

        injectImageFilename(template, props.getNode().getFaceInputId(), faceImageFilename);
        injectImageFilename(template, props.getNode().getGarmentInputId(), garmentImageFilename);

        return template;
    }

    /**
     * Navigates to prompt[nodeId].inputs.image and sets the filename.
     * This is the "modular" part — node IDs come from config, not hardcoded.
     */
    private void injectImageFilename(ObjectNode workflow, String nodeId, String filename) {
        JsonNode node = workflow.get(nodeId);
        if (node == null || !node.has("inputs")) {
            throw new IllegalStateException(
                "ComfyUI workflow template has no node with id='" + nodeId + "'. " +
                "Check comfyui.node.face-input-id / garment-input-id in application.properties."
            );
        }
        ((ObjectNode) node.get("inputs")).put("image", filename);
    }

    // -------------------------------------------------------------------------
    // Step 2: POST to ComfyUI /prompt
    // -------------------------------------------------------------------------

    private String submitPrompt(ObjectNode workflow) {
        Map<String, Object> payload = Map.of(
            "prompt", workflow,
            "client_id", props.getClientId()
        );

        JsonNode response = webClient.post()
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

    // -------------------------------------------------------------------------
    // Step 3: Poll GET /history/{promptId} until the job is done
    // -------------------------------------------------------------------------

    private String pollForOutputImage(String promptId) throws InterruptedException {
        int maxAttempts = props.getPolling().getMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Thread.sleep(2000);

            JsonNode history = webClient.get()
                .uri("/history/{promptId}", promptId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (history == null || !history.has(promptId)) {
                log.debug("Poll attempt {}/{}: job not in history yet.", attempt, maxAttempts);
                continue;
            }

            JsonNode job = history.get(promptId);
            JsonNode status = job.path("status");

            if (status.path("completed").asBoolean(false)) {
                String outputFilename = extractOutputFilename(job);
                log.info("ComfyUI render complete. output={}", outputFilename);
                return outputFilename;
            }

            String statusStr = status.path("status_str").asText("running");
            log.debug("Poll attempt {}/{}: status={}", attempt, maxAttempts, statusStr);
        }

        log.warn("ComfyUI polling timed out after {} attempts for prompt_id={}", maxAttempts, promptId);
        return null;
    }

    /**
     * Extracts the first output image filename from the history response.
     * Looks inside history[promptId].outputs[outputNodeId].images[0].filename
     */
    private String extractOutputFilename(JsonNode job) {
        String outputNodeId = props.getNode().getOutputNodeId();

        JsonNode images = job.path("outputs")
            .path(outputNodeId)
            .path("images");

        if (images.isArray() && !images.isEmpty()) {
            return images.get(0).path("filename").asText();
        }

        throw new IllegalStateException(
            "ComfyUI job completed but no images found at outputs[" + outputNodeId + "]. " +
            "Check comfyui.node.output-node-id in application.properties."
        );
    }

    // -------------------------------------------------------------------------
    // Utility: build the /view URL for a completed output image
    // -------------------------------------------------------------------------

    public String buildViewUrl(String filename) {
        return props.getBaseUrl() + "/view?filename=" + filename + "&type=output";
    }

}
