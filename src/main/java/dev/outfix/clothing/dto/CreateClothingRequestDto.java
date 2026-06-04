package dev.outfix.clothing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClothingRequestDto {

    @Min(1)
    private int tokenFormalitas;

    @NotBlank
    private String tags;
}
