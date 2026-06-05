package dev.outfix.recommendation.dto;

import dev.outfix.clothing.dto.ClothingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A single recommended outfit: one top + one pair of pants, matched to a schedule's
 * formality token (and dresscode, if any).
 *
 * status:
 *   ok            — both top and bottom were found
 *   missing_top   — a matching pants was found but no top
 *   missing_bottom— a matching top was found but no pants
 *   empty         — nothing matched at all
 */
@Getter
@AllArgsConstructor
public class OutfitRecommendationResponseDto {

    private String status;
    private ClothingResponseDto top;
    private ClothingResponseDto bottom;

    public static OutfitRecommendationResponseDto of(
            ClothingResponseDto top, ClothingResponseDto bottom) {
        String status;
        if (top != null && bottom != null) {
            status = "ok";
        } else if (top == null && bottom == null) {
            status = "empty";
        } else if (top == null) {
            status = "missing_top";
        } else {
            status = "missing_bottom";
        }
        return new OutfitRecommendationResponseDto(status, top, bottom);
    }
}
