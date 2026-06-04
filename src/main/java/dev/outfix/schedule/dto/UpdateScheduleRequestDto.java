package dev.outfix.schedule.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * The data the client sends when updating a scheduled activity.
 * All fields are optional — only provided fields will be updated.
 */
@Getter
@Setter
public class UpdateScheduleRequestDto {

    /** New activity name. Leave null to keep the current value. */
    private String activityName;

    /** New required formality level. Leave null to keep the current value. */
    private Integer targetToken;

    /** New required tag. Leave null to keep the current value. */
    private String targetTag;

    /** New dresscode. Leave null to keep the current value. Pass empty string "" to clear it. */
    private String dresscode;
}
