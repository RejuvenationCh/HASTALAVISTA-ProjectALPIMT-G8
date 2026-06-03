package dev.outfix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wardrobes")
@Getter
@Setter
@NoArgsConstructor
public class Wardrobe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "clothing_image_url")
    private String clothingImageUrl;

    @Column(name = "token_formalitas")
    private Integer tokenFormalitas;

    /**
     * Comma-separated tag string, e.g. "Men,Top".
     * Queried with LIKE for MVP matching logic.
     */
    @Column(length = 500)
    private String tags;

}
