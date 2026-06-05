package dev.outfix.wardrobe.entity;

import java.time.LocalDateTime;

import dev.outfix.clothing.entity.Clothing;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A saved outfit — a combination of clothing items chosen from the user's
 * clothing collection, linked to a schedule activity, with a ComfyUI mockup
 * generated in the background after saving.
 */
@Entity
@Table(name = "wardrobes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wardrobe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The schedule activity this outfit was created for. Nullable — can be a standalone outfit. */
    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "top_clothing_id")
    private Clothing topClothing;

    @ManyToOne
    @JoinColumn(name = "bottom_clothing_id")
    private Clothing bottomClothing;

    @ManyToOne
    @JoinColumn(name = "shoes_clothing_id")
    private Clothing shoesClothing;

    /** URL to the ComfyUI-generated JPG mockup. Populated after generation completes. */
    @Column(name = "mockup_jpg_url")
    private String mockupJpgUrl;

    /** URL to the background-removed PNG mockup. Populated after generation completes. */
    @Column(name = "mockup_png_url")
    private String mockupPngUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WardrobeStatus status;

    @Column(name = "favorite", nullable = false)
    @Builder.Default
    private boolean favorite = false;

    private LocalDateTime createdAt;
}
