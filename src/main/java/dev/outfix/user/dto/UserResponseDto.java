package dev.outfix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The user profile data sent back to the client.
 * Excludes sensitive fields like password.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;

    /** URL to the user's uploaded face/body photo. Null if not uploaded yet. */
    private String faceModelUrl;

    /** Whether the user has completed the onboarding tutorial. */
    private boolean finishedTutorial;
}
