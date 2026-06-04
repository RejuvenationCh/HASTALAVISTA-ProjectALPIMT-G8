package dev.outfix.wardrobe.entity;

import java.time.LocalDateTime;

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
 * Represents a single clothing item in a user's wardrobe.
 * Maps to the "wardrobes" table in the database.
 */
@Entity
@Table(name = "wardrobes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wardrobe {

    /** Auto-generated unique ID for each wardrobe item. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who owns this clothing item. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** URL path to the uploaded photo of the clothing item. */
    @Column(name = "clothing_image_url")
    private String clothingImageUrl;

    /**
     * Formality level assigned by the user (e.g. 1 = casual, 5 = formal).
     * Used to match clothing items to schedule requirements.
     */
    @Column(name = "token_formalitas", nullable = false)
    private int tokenFormalitas;

    /**
     * Comma-separated tags describing the item (e.g. "Men,Top,Casual").
     * Used for filtering during outfit recommendation.
     */
    @Column(name = "tags")
    private String tags;

    /** Timestamp of when this item was added to the wardrobe. */
    private LocalDateTime createdAt;
}
