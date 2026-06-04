package dev.outfix.comfyui;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * All ComfyUI-related configuration values from application.properties.
 *
 * Every field here maps to a "comfyui.*" property.
 * Node IDs match the node numbers inside the workflow JSON template files.
 * If a workflow is edited and node numbers change, update application.properties only —
 * no code changes needed.
 */
@ConfigurationProperties(prefix = "comfyui")
@Getter
@Setter
public class ComfyUiProperties {

    /** The base URL of the running ComfyUI server. */
    private String baseUrl = "http://localhost:8188";

    /** A client identifier sent with every prompt request to ComfyUI. */
    private String clientId = "outfix";

    /** Node ID configuration for workflow templates. */
    private Node node = new Node();

    /** Polling configuration for waiting on render completion. */
    private Polling polling = new Polling();

    /**
     * Maps each logical image slot to its ComfyUI node ID.
     * These must match the actual node IDs in the workflow JSON files.
     */
    @Getter
    @Setter
    public static class Node {

        // --- Single garment workflow (workflow_template.json) ---

        /** Node ID of the LoadImage node for the face/body photo. */
        private String faceInputId = "295";

        /** Node ID of the LoadImage node for the garment photo. */
        private String garmentInputId = "297";

        /** Node ID of the segmentation node (controls which body part is replaced). */
        private String segmentNodeId = "307";

        /** Node ID of the CatVTON virtual try-on node. */
        private String catvtonNodeId = "305";

        /** Node ID of the SaveImage output node. */
        private String outputNodeId = "163";

        // --- Full outfit workflow (workflow_full_outfit_template.json) ---

        /** Node ID of the LoadImage node for the top garment. */
        private String topGarmentInputId = "310";

        /** Node ID of the LoadImage node for the bottom garment. */
        private String bottomGarmentInputId = "311";

        /** Node ID of the LoadImage node for the shoes. */
        private String shoesGarmentInputId = "312";

        /** Node ID of the second CatVTON pass (bottom garment). */
        private String catvtonPass2NodeId = "316";

        /** Node ID of the third CatVTON pass (shoes). */
        private String catvtonPass3NodeId = "318";

        /** Node ID of the final output node in the full outfit workflow. */
        private String fullOutfitOutputNodeId = "163";

        /**
         * Node ID of the second output in the full outfit workflow.
         * This output runs the result through RMBG to remove the background,
         * producing a transparent PNG alongside the regular JPG.
         */
        private String pngOutputNodeId = "331";
    }

    /**
     * Controls how long the server waits for a render to finish.
     * Each poll attempt waits 2 seconds. maxAttempts * 2 = total wait time in seconds.
     */
    @Getter
    @Setter
    public static class Polling {

        /** Maximum number of times to check for completion before giving up. */
        private int maxAttempts = 30;
    }
}
