package dev.outfix.schedule.entity;

import java.time.LocalDate;

import dev.outfix.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a scheduled activity that requires an outfit.
 * The system uses targetToken and targetTag to find matching wardrobe items.
 * Maps to the "schedules" table in the database.
 */
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    /** Auto-generated unique ID for each schedule. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who created this schedule. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** A short description of the activity (e.g. "Job Interview", "Gym"). */
    @Column(name = "activity_name", nullable = false)
    private String activityName;

    /** The calendar date this activity is scheduled for. Drives the schedule calendar. */
    @Column(name = "event_date")
    private LocalDate eventDate;

    /**
     * The required formality level for this activity.
     * Must match the tokenFormalitas of wardrobe items to appear in results.
     */
    @Column(name = "target_token", nullable = false)
    private int targetToken;

    /**
     * The clothing category/tag required for this activity (e.g. "Men", "Top").
     * Used to filter wardrobe items during recommendation.
     */
    @Column(name = "target_tag")
    private String targetTag;

    /**
     * Optional specific dresscode required for this activity (e.g. "Batik", "Sportswear").
     * Null means no special dresscode — formality token and category tag are sufficient.
     */
    @Column(name = "dresscode")
    private String dresscode;
}
