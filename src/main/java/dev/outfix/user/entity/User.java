package dev.outfix.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a registered user in the system.
 * Maps to the "users" table in the database.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Auto-generated unique ID for each user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The display name chosen by the user. */
    @Column(nullable = false)
    private String username;

    /** The user's email address, used for login. Must be unique. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never store plain text. */
    @Column(nullable = false)
    private String password;

    /** User's role (e.g. "USER"). Used by Spring Security. */
    @Column(nullable = false)
    private String role;

    /** URL path to the user's uploaded face/body photo. */
    @Column(name = "face_model_url")
    private String faceModelUrl;

    /** Tracks whether the user has completed the onboarding tutorial. */
    @Column(name = "finished_tutorial", nullable = false)
    @Builder.Default
    private boolean finishedTutorial = false;

    /** Timestamp of when the account was created. */
    private LocalDateTime createdAt;
}
