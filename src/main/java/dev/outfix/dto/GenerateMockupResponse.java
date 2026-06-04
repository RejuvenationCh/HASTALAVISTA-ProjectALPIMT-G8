package dev.outfix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The response sent back after a mockup generation request.
 *
 * Possible status values:
 *   "success" — imageUrl (JPG) and pngImageUrl (background-removed PNG) are populated.
 *   "timeout" — ComfyUI took too long; the client should try again.
 *   "error"   — something went wrong; message explains what.
 */
@Getter
@AllArgsConstructor
public class GenerateMockupResponse {

    /** One of: "success", "timeout", "error". */
    private String status;

    /** URL to the final JPG result. Only present when status is "success". */
    private String imageUrl;

    /**
     * URL to the background-removed PNG version of the result.
     * Only present on full-outfit generation when status is "success".
     */
    private String pngImageUrl;

    /** Human-readable explanation. Only present when status is "error" or "timeout". */
    private String message;

    /** Use this for single-garment — only a JPG output is available. */
    public static GenerateMockupResponse success(String jpgUrl) {
        return new GenerateMockupResponse("success", jpgUrl, null, null);
    }

    /** Use this for full-outfit — both JPG and background-removed PNG are available. */
    public static GenerateMockupResponse successWithBoth(String jpgUrl, String pngUrl) {
        return new GenerateMockupResponse("success", jpgUrl, pngUrl, null);
    }

    /** Use this when ComfyUI did not finish within the polling time limit. */
    public static GenerateMockupResponse timeout() {
        return new GenerateMockupResponse("timeout", null, null,
                "ComfyUI render timed out. Try again later.");
    }

    /** Use this when an error occurred during generation. */
    public static GenerateMockupResponse error(String message) {
        return new GenerateMockupResponse("error", null, null, message);
    }
}
