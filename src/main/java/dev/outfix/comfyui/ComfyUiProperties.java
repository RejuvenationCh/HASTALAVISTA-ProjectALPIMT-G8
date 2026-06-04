package dev.outfix.comfyui;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "comfyui")
@Getter
@Setter
public class ComfyUiProperties {

    private String baseUrl = "http://localhost:8188";
    private String clientId = "outfix";
    private Node node = new Node();
    private Polling polling = new Polling();

    @Getter
    @Setter
    public static class Node {
        // --- Single garment (workflow_template.json) ---
        private String faceInputId    = "295";
        private String garmentInputId = "297";   // LoadImage garment in single workflow
        private String segmentNodeId  = "307";
        private String catvtonNodeId  = "305";
        private String outputNodeId   = "163";

        // --- Full outfit (workflow_full_outfit_template.json) ---
        private String topGarmentInputId    = "310";
        private String bottomGarmentInputId = "311";
        private String shoesGarmentInputId  = "312";
        private String catvtonPass2NodeId   = "316";
        private String catvtonPass3NodeId   = "318";
        private String fullOutfitOutputNodeId = "163";
    }

    @Getter
    @Setter
    public static class Polling {
        private int maxAttempts = 30;
    }
}
