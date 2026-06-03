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
        private String faceInputId = "1";
        private String garmentInputId = "2";
        private String outputNodeId = "9";
    }

    @Getter
    @Setter
    public static class Polling {
        private int maxAttempts = 30;
    }

}
