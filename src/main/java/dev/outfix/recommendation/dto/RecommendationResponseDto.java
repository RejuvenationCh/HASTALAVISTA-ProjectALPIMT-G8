package dev.outfix.recommendation.dto;

import java.util.List;

import dev.outfix.clothing.dto.ClothingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendationResponseDto {

    private String status;
    private String action;
    private List<ClothingResponseDto> items;

    public static RecommendationResponseDto found(List<ClothingResponseDto> items) {
        return new RecommendationResponseDto("ok", null, items);
    }

    public static RecommendationResponseDto empty() {
        return new RecommendationResponseDto("empty", "suggest_input", null);
    }
}
