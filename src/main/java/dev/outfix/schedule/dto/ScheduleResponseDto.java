package dev.outfix.schedule.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The schedule data sent back to the client.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {

    private Long id;

    /** Short description of the activity. */
    private String activityName;

    /** The calendar date for this activity (ISO yyyy-MM-dd). */
    private LocalDate eventDate;

    /** The formality level required to match wardrobe items. */
    private int targetToken;

    /** The tag/category required to match wardrobe items. */
    private String targetTag;

    /** Optional dresscode required for this activity. Null if none. */
    private String dresscode;
}
