package dev.outfix.recommendation.dto;

import java.util.List;

import dev.outfix.clothing.dto.ClothingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** All clothing items that match a schedule's formality + dresscode, grouped by slot. */
@Getter
@AllArgsConstructor
public class OutfitOptionsResponseDto {
    private List<ClothingResponseDto> tops;
    private List<ClothingResponseDto> bottoms;
}
