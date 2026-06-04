package dev.outfix.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The data sent back to the client after a successful login or registration.
 * The client stores the token and sends it with every future request.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    /** JWT token — the client must include this in the Authorization header. */
    private String token;

    /** The logged-in user's database ID. */
    private Long userId;

    /** The logged-in user's display name. */
    private String username;

    /** Whether the user has completed the onboarding tutorial. */
    private boolean finishedTutorial;
}
