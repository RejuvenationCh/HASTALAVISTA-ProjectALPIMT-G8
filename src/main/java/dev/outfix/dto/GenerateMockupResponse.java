package dev.outfix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenerateMockupResponse {
    private String status;
    private String imageUrl;
    private String message;

    public static GenerateMockupResponse success(String imageUrl) {
        return new GenerateMockupResponse("success", imageUrl, null);
    }

    public static GenerateMockupResponse timeout() {
        return new GenerateMockupResponse("timeout", null, "ComfyUI render timed out. Try again later.");
    }

    public static GenerateMockupResponse error(String message) {
        return new GenerateMockupResponse("error", null, message);
    }
}
