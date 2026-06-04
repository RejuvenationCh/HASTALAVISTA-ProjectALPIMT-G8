package dev.outfix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The response sent back after a mockup generation request.
 *
 * Possible status values:
 *   "success" — imageUrl contains the link to the generated image.
 *   "timeout" — ComfyUI took too long; the client should try again.
 *   "error"   — something went wrong; message explains what.
 */
@Getter
@AllArgsConstructor
public class GenerateMockupResponse {

    /** One of: "success", "timeout", "error". */
    private String status;

    /** URL to view the generated image. Only present when status is "success". */
    private String imageUrl;

    /** Human-readable explanation. Only present when status is "error" or "timeout". */
    private String message;

    /** Use this when the image was generated successfully. */
    public static GenerateMockupResponse success(String imageUrl) {
        return new GenerateMockupResponse("success", imageUrl, null);
    }

    /** Use this when ComfyUI did not finish within the polling time limit. */
    public static GenerateMockupResponse timeout() {
        return new GenerateMockupResponse("timeout", null,
                "ComfyUI render timed out. Try again later.");
    }

    /** Use this when an error occurred during generation. */
    public static GenerateMockupResponse error(String message) {
        return new GenerateMockupResponse("error", null, message);
    }
}
