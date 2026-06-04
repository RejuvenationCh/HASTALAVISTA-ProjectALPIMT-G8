package dev.outfix.wardrobe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * The data the client sends when adding a new clothing item.
 * The image file is sent separately as a multipart upload field named "file".
 */
@Getter
@Setter
public class CreateWardrobeRequestDto {

    /** Formality level from 1 (very casual) to any number (more formal). */
    @Min(1)
    private int tokenFormalitas;

    /**
     * Tags describing the item, separated by commas.
     * Example: "Men,Top,Casual"
     */
    @NotBlank
    private String tags;
}
