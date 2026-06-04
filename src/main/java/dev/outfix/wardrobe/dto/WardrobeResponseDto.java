package dev.outfix.wardrobe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeResponseDto {

    private Long id;
    private String clothingImageUrl;
    private int tokenFormalitas;
    private String tags;
}
