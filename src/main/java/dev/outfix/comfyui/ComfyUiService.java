package dev.outfix.comfyui;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles all communication with the ComfyUI image generation server.
 *
 * Flow for every generation request:
 *   1. Load a JSON workflow template from the classpath.
 *   2. Inject the image filenames and a random seed into the workflow.
 *   3. POST the workflow to ComfyUI's /prompt endpoint to start rendering.
 *   4. Poll /history until the render is done, then return the output filename.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyUiService {

    private final ComfyUiProperties props;
    private final ObjectMapper objectMapper;
    private final WebClient comfyUiWebClient;

    private final Random random = new Random();

    // -----------------------------------------------------------------------
    // Single garment try-on
    // -----------------------------------------------------------------------

    /**
     * Generates a single-garment try-on mockup using workflow_template.json.
     *
     * @param faceFilename     filename already uploaded to ComfyUI's input folder
     * @param garmentFilename  filename already uploaded to ComfyUI's input folder
     * @param clothingType     one of: TOP, BOTTOM, DRESS, SHOES
     * @return                 the output image filename from ComfyUI, or null on timeout
     */
    public String generateMockup(String faceFilename, String garmentFilename,
            String clothingType) throws IOException, InterruptedException {

        ObjectNode workflow = buildSingleGarmentWorkflow(
                faceFilename, garmentFilename, clothingType);

        String promptId = submitPromptToComfyUi(workflow);
        log.info("Single-garment job submitted. prompt_id={}", promptId);

        return pollUntilComplete(promptId, props.getNode().getOutputNodeId());
    }

    private ObjectNode buildSingleGarmentWorkflow(String faceFilename,
            String garmentFilename, String clothingType) throws IOException {

        ComfyUiProperties.Node nodeIds = props.getNode();
        ObjectNode workflow = loadWorkflowTemplate("comfyui/workflow_template.json");

        setNodeImageFilename(workflow, nodeIds.getFaceInputId(), faceFilename);
        setNodeImageFilename(workflow, nodeIds.getGarmentInputId(), garmentFilename);
        randomizeSeed(workflow, nodeIds.getCatvtonNodeId());
        applyClothingTypeToSegmentNode(
                getNodeInputs(workflow, nodeIds.getSegmentNodeId()), clothingType);

        return workflow;
    }

    // -----------------------------------------------------------------------
    // Full outfit try-on (top + bottom + shoes, three sequential CatVTON passes)
    // -----------------------------------------------------------------------

    /**
     * Generates a full outfit try-on mockup using workflow_full_outfit_template.json.
     * Runs three CatVTON passes in sequence: top → bottom → shoes.
     *
     * @return String[2] where [0] = JPG filename, [1] = PNG filename.
     *         Both are null if the job timed out.
     */
    public String[] generateFullOutfit(String faceFilename, String topFilename,
            String bottomFilename, String shoesFilename)
            throws IOException, InterruptedException {

        ObjectNode workflow = buildFullOutfitWorkflow(
                faceFilename, topFilename, bottomFilename, shoesFilename);

        String promptId = submitPromptToComfyUi(workflow);
        log.info("Full-outfit job submitted. prompt_id={}", promptId);

        return pollForBothOutputs(promptId,
                props.getNode().getFullOutfitOutputNodeId(),
                props.getNode().getPngOutputNodeId());
    }

    private ObjectNode buildFullOutfitWorkflow(String faceFilename,
            String topFilename, String bottomFilename, String shoesFilename)
            throws IOException {

        ComfyUiProperties.Node nodeIds = props.getNode();
        ObjectNode workflow = loadWorkflowTemplate(
                "comfyui/workflow_full_outfit_template.json");

        // Inject the four image filenames into their respective LoadImage nodes
        setNodeImageFilename(workflow, nodeIds.getFaceInputId(), faceFilename);
        setNodeImageFilename(workflow, nodeIds.getTopGarmentInputId(), topFilename);
        setNodeImageFilename(workflow, nodeIds.getBottomGarmentInputId(), bottomFilename);
        setNodeImageFilename(workflow, nodeIds.getShoesGarmentInputId(), shoesFilename);

        // Each CatVTON pass gets its own random seed so every run produces a unique image
        randomizeSeed(workflow, nodeIds.getCatvtonNodeId());
        randomizeSeed(workflow, nodeIds.getCatvtonPass2NodeId());
        randomizeSeed(workflow, nodeIds.getCatvtonPass3NodeId());

        return workflow;
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    /** Reads a workflow JSON template file from src/main/resources. */
    private ObjectNode loadWorkflowTemplate(String classpathPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        return (ObjectNode) objectMapper.readTree(resource.getInputStream());
    }

    /**
     * Sets the "image" field on a LoadImage node inside the workflow.
     * Throws a clear error if the node ID doesn't exist in the template.
     */
    private void setNodeImageFilename(ObjectNode workflow, String nodeId,
            String filename) {
        if (workflow.get(nodeId) == null) {
            throw new IllegalStateException(
                    "Workflow template is missing node id='" + nodeId + "'. "
                    + "Check comfyui.node.* in application.properties.");
        }
        getNodeInputs(workflow, nodeId).put("image", filename);
    }

    /** Replaces the seed on a node with a new random number for unique outputs. */
    private void randomizeSeed(ObjectNode workflow, String nodeId) {
        getNodeInputs(workflow, nodeId).put("seed", random.nextLong(1_000_000_000L));
    }

    /** Returns the "inputs" object of a given node for easy field editing. */
    private ObjectNode getNodeInputs(ObjectNode workflow, String nodeId) {
        return (ObjectNode) workflow.get(nodeId).get("inputs");
    }

    /**
     * Sets the correct body-part flags on the segmentation node.
     * ComfyUI needs to know which part of the body to replace with the garment.
     */
    private void applyClothingTypeToSegmentNode(ObjectNode segmentInputs,
            String clothingType) {

        // Reset all flags to false first, then enable only the relevant ones
        segmentInputs.put("Upper-clothes", false);
        segmentInputs.put("Dress",         false);
        segmentInputs.put("Pants",         false);
        segmentInputs.put("Skirt",         false);
        segmentInputs.put("Left-shoe",     false);
        segmentInputs.put("Right-shoe",    false);
        segmentInputs.put("Scarf",         false);

        switch (clothingType.toUpperCase()) {
            case "TOP"    -> segmentInputs.put("Upper-clothes", true);
            case "BOTTOM" -> {
                segmentInputs.put("Pants", true);
                segmentInputs.put("Skirt", true);
            }
            case "DRESS"  -> segmentInputs.put("Dress", true);
            case "SHOES"  -> {
                segmentInputs.put("Left-shoe", true);
                segmentInputs.put("Right-shoe", true);
            }
            // Default to upper body if clothingType is unrecognised
            default -> segmentInputs.put("Upper-clothes", true);
        }
    }

    /**
     * Sends the completed workflow to ComfyUI's /prompt endpoint to start rendering.
     * Returns the prompt_id which is used to track the job status.
     */
    private String submitPromptToComfyUi(ObjectNode workflow) {
        Map<String, Object> requestBody = Map.of(
                "prompt",    workflow,
                "client_id", props.getClientId());

        JsonNode comfyResponse = comfyUiWebClient.post()
                .uri("/prompt")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (comfyResponse == null || !comfyResponse.has("prompt_id")) {
            throw new IllegalStateException(
                    "ComfyUI did not return a prompt_id. Response: " + comfyResponse);
        }

        return comfyResponse.get("prompt_id").asText();
    }

    /**
     * Repeatedly checks ComfyUI's /history endpoint every 2 seconds
     * until the job is complete or the max attempt limit is reached.
     *
     * @return the output image filename, or null if timed out
     */
    private String pollUntilComplete(String promptId, String outputNodeId)
            throws InterruptedException {

        int maxAttempts = props.getPolling().getMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Thread.sleep(2000);

            JsonNode history = comfyUiWebClient.get()
                    .uri("/history/{promptId}", promptId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (history == null || !history.has(promptId)) {
                log.debug("Poll {}/{}: job not in history yet.", attempt, maxAttempts);
                continue;
            }

            JsonNode jobResult = history.get(promptId);
            boolean isComplete = jobResult.path("status")
                    .path("completed").asBoolean(false);

            if (isComplete) {
                String outputFilename = extractOutputFilename(jobResult, outputNodeId);
                log.info("Render complete. output_file={}", outputFilename);
                return outputFilename;
            }

            log.debug("Poll {}/{}: status={}", attempt, maxAttempts,
                    jobResult.path("status").path("status_str").asText("running"));
        }

        log.warn("ComfyUI render timed out after {} attempts for prompt_id={}",
                maxAttempts, promptId);
        return null;
    }

    /**
     * Like pollUntilComplete but extracts two output filenames from the same job.
     * Returns String[2]: [0] = JPG filename, [1] = PNG filename.
     * Both are null if the job timed out.
     */
    private String[] pollForBothOutputs(String promptId,
            String jpgNodeId, String pngNodeId) throws InterruptedException {

        int maxAttempts = props.getPolling().getMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Thread.sleep(2000);

            JsonNode history = comfyUiWebClient.get()
                    .uri("/history/{promptId}", promptId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (history == null || !history.has(promptId)) {
                log.debug("Poll {}/{}: job not in history yet.", attempt, maxAttempts);
                continue;
            }

            JsonNode jobResult = history.get(promptId);
            boolean isComplete = jobResult.path("status")
                    .path("completed").asBoolean(false);

            if (isComplete) {
                String jpgFilename = extractOutputFilename(jobResult, jpgNodeId);
                String pngFilename = extractOutputFilename(jobResult, pngNodeId);
                log.info("Full-outfit render complete. jpg={}, png={}", jpgFilename, pngFilename);
                return new String[]{ jpgFilename, pngFilename };
            }

            log.debug("Poll {}/{}: status={}", attempt, maxAttempts,
                    jobResult.path("status").path("status_str").asText("running"));
        }

        log.warn("ComfyUI render timed out after {} attempts for prompt_id={}",
                maxAttempts, promptId);
        return new String[]{ null, null };
    }

    /** Reads the output image filename from the completed job's history entry. */
    private String extractOutputFilename(JsonNode jobResult, String outputNodeId) {
        JsonNode outputImages = jobResult.path("outputs")
                .path(outputNodeId).path("images");

        if (outputImages.isArray() && !outputImages.isEmpty()) {
            return outputImages.get(0).path("filename").asText();
        }

        throw new IllegalStateException(
                "Render finished but no image found at outputs[" + outputNodeId + "]. "
                + "Check comfyui.node.output-node-id in application.properties.");
    }

    /**
     * Builds the URL to view a finished output image directly from ComfyUI.
     * Example: http://localhost:8188/view?filename=result.png&type=output
     */
    public String buildViewUrl(String outputFilename) {
        return props.getBaseUrl() + "/view?filename=" + outputFilename + "&type=output";
    }
}
