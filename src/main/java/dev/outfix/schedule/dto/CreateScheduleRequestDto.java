package dev.outfix.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * The data the client sends when creating a new scheduled activity.
 */
@Getter
@Setter
public class CreateScheduleRequestDto {

    /** Short description of the activity (e.g. "Job Interview", "Wedding"). */
    @NotBlank
    private String activityName;

    /** The required formality level for this activity (must be at least 1). */
    @Min(1)
    private int targetToken;

    /** The clothing category needed for this activity (e.g. "Men", "Formal"). */
    @NotBlank
    private String targetTag;
}
