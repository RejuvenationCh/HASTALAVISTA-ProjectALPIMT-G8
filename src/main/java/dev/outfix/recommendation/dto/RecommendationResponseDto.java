package dev.outfix.recommendation.dto;

import java.util.List;

import dev.outfix.wardrobe.dto.WardrobeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Returned by the recommendation endpoint.
 *
 * status = "ok"    → items contains matching wardrobe entries
 * status = "empty" → no matches; action = "suggest_input" tells the UI what to show
 */
@Getter
@AllArgsConstructor
public class RecommendationResponseDto {

    private String status;
    private String action;
    private List<WardrobeResponseDto> items;

    public static RecommendationResponseDto found(List<WardrobeResponseDto> items) {
        return new RecommendationResponseDto("ok", null, items);
    }

    public static RecommendationResponseDto empty() {
        return new RecommendationResponseDto("empty", "suggest_input", null);
    }
}
