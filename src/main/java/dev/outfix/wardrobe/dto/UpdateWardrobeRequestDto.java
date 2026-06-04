package dev.outfix.wardrobe.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * The data the client sends when updating an existing clothing item.
 * All fields are optional — only the provided fields will be updated.
 */
@Getter
@Setter
public class UpdateWardrobeRequestDto {

    /** New formality level. Leave null to keep the current value. */
    private Integer tokenFormalitas;

    /** New tags. Leave null to keep the current value. */
    private String tags;
}
