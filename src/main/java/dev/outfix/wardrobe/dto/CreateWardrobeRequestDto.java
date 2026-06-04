package dev.outfix.wardrobe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWardrobeRequestDto {

    private String clothingImageUrl;

    @Min(1)
    private int tokenFormalitas;

    @NotBlank
    private String tags;
}
