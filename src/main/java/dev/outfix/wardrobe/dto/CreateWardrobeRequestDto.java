package dev.outfix.wardrobe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWardrobeRequestDto {

    @NotBlank
    private String name;

    private String description;
}