package dev.outfix.clothing.entity;

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

@Entity
@Table(name = "clothing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clothing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "clothing_image_url")
    private String clothingImageUrl;

    @Column(name = "token_formalitas", nullable = false)
    private int tokenFormalitas;

    @Column(name = "tags")
    private String tags;

    private LocalDateTime createdAt;
}
