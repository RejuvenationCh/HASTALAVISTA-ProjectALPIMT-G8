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

    // -------------------------------------------------------------------------
    // Single garment
    // -------------------------------------------------------------------------

    public String generateMockup(String faceFilename, String garmentFilename, String clothingType)
            throws IOException, InterruptedException {

        ObjectNode workflow = buildSingleWorkflow(faceFilename, garmentFilename, clothingType);
        String promptId = submitPrompt(workflow);
        log.info("Single-garment submitted. prompt_id={}", promptId);
        return pollForOutputImage(promptId, props.getNode().getOutputNodeId());
    }

    private ObjectNode buildSingleWorkflow(String faceFilename, String garmentFilename, String clothingType)
            throws IOException {

        ComfyUiProperties.Node n = props.getNode();
        ObjectNode workflow = loadTemplate("comfyui/workflow_template.json");

        setImageFilename(workflow, n.getFaceInputId(),    faceFilename);
        setImageFilename(workflow, n.getGarmentInputId(), garmentFilename);
        randomizeSeed(workflow, n.getCatvtonNodeId());
        applyClothingType(inputs(workflow, n.getSegmentNodeId()), clothingType);

        return workflow;
    }

    // -------------------------------------------------------------------------
    // Full outfit — 3 sequential CatVTON passes (top → bottom → shoes)
    // RMBG flags are fixed in the template; Spring Boot only injects filenames + seeds.
    // -------------------------------------------------------------------------

    public String generateFullOutfit(String faceFilename,
                                     String topFilename,
                                     String bottomFilename,
                                     String shoesFilename)
            throws IOException, InterruptedException {

        ObjectNode workflow = buildFullOutfitWorkflow(faceFilename, topFilename, bottomFilename, shoesFilename);
        String promptId = submitPrompt(workflow);
        log.info("Full-outfit submitted. prompt_id={}", promptId);
        return pollForOutputImage(promptId, props.getNode().getFullOutfitOutputNodeId());
    }

    private ObjectNode buildFullOutfitWorkflow(String faceFilename,
                                               String topFilename,
                                               String bottomFilename,
                                               String shoesFilename)
            throws IOException {

        ComfyUiProperties.Node n = props.getNode();
        ObjectNode workflow = loadTemplate("comfyui/workflow_full_outfit_template.json");

        // Inject the 4 image filenames
        setImageFilename(workflow, n.getFaceInputId(),           faceFilename);
        setImageFilename(workflow, n.getTopGarmentInputId(),     topFilename);
        setImageFilename(workflow, n.getBottomGarmentInputId(),  bottomFilename);
        setImageFilename(workflow, n.getShoesGarmentInputId(),   shoesFilename);

        // Randomize seeds on all three CatVTON passes so each run is unique
        randomizeSeed(workflow, n.getCatvtonNodeId());
        randomizeSeed(workflow, n.getCatvtonPass2NodeId());
        randomizeSeed(workflow, n.getCatvtonPass3NodeId());

        return workflow;
        // Note: RMBG flags (307, 315, 319) are fixed in the template —
        // 307 targets top, 315 targets bottom, 319 targets shoes.
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private ObjectNode loadTemplate(String classpathPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        return (ObjectNode) objectMapper.readTree(resource.getInputStream());
    }

    private void setImageFilename(ObjectNode workflow, String nodeId, String filename) {
        JsonNode node = workflow.get(nodeId);
        if (node == null) {
            throw new IllegalStateException(
                "Workflow template missing node id='" + nodeId + "'. " +
                "Check comfyui.node.* in application.properties.");
        }
        inputs(workflow, nodeId).put("image", filename);
    }

    private void randomizeSeed(ObjectNode workflow, String nodeId) {
        inputs(workflow, nodeId).put("seed", random.nextLong(1_000_000_000L));
    }

    private ObjectNode inputs(ObjectNode workflow, String nodeId) {
        return (ObjectNode) workflow.get(nodeId).get("inputs");
    }

    private void applyClothingType(ObjectNode segInputs, String clothingType) {
        segInputs.put("Upper-clothes", false);
        segInputs.put("Dress",         false);
        segInputs.put("Pants",         false);
        segInputs.put("Skirt",         false);
        segInputs.put("Left-shoe",     false);
        segInputs.put("Right-shoe",    false);
        segInputs.put("Scarf",         false);

        switch (clothingType.toUpperCase()) {
            case "TOP"    -> segInputs.put("Upper-clothes", true);
            case "BOTTOM" -> { segInputs.put("Pants", true); segInputs.put("Skirt", true); }
            case "DRESS"  -> segInputs.put("Dress", true);
            case "SHOES"  -> { segInputs.put("Left-shoe", true); segInputs.put("Right-shoe", true); }
            default       -> segInputs.put("Upper-clothes", true);
        }
    }

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

    private String pollForOutputImage(String promptId, String outputNodeId) throws InterruptedException {
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
                String filename = extractOutputFilename(job, outputNodeId);
                log.info("Render complete. output={}", filename);
                return filename;
            }

            log.debug("Poll {}/{}: status={}", attempt, maxAttempts,
                job.path("status").path("status_str").asText("running"));
        }

        log.warn("ComfyUI timed out after {} attempts for prompt_id={}", maxAttempts, promptId);
        return null;
    }

    private String extractOutputFilename(JsonNode job, String outputNodeId) {
        JsonNode images = job.path("outputs").path(outputNodeId).path("images");

        if (images.isArray() && !images.isEmpty()) {
            return images.get(0).path("filename").asText();
        }

        throw new IllegalStateException(
            "Render finished but no image found at outputs[" + outputNodeId + "]. " +
            "Check comfyui.node.output-node-id in application.properties.");
    }

    public String buildViewUrl(String filename) {
        return props.getBaseUrl() + "/view?filename=" + filename + "&type=output";
    }
}
