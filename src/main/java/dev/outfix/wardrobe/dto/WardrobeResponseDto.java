package dev.outfix.wardrobe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The clothing item data sent back to the client.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeResponseDto {

    private Long id;

    /** URL to the clothing item's photo. Null if no photo was uploaded. */
    private String clothingImageUrl;

    /** Formality level assigned by the user. */
    private int tokenFormalitas;

    /** Tags describing the item (e.g. "Men,Top,Casual"). */
    private String tags;
}
