package dev.outfix.wardrobe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWardrobeRequestDto {

    /** ID of the schedule activity this outfit is for. Nullable. */
    private Long scheduleId;

    private Long topClothingId;
    private Long bottomClothingId;
    private Long shoesClothingId;

    /** ComfyUI filename of a one-time face photo. When set, overrides the user's saved face model for this generation only. */
    private String tempFaceComfyFilename;
}
