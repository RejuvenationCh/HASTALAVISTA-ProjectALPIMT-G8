package dev.outfix.user.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.outfix.user.dto.UserResponseDto;
import dev.outfix.user.entity.User;
import dev.outfix.user.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * Handles HTTP requests related to the logged-in user's profile.
 * All endpoints here require a valid JWT token.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/me
     * Returns the profile of the currently logged-in user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            Authentication auth) {

        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * PATCH /api/users/tutorial
     * Marks the tutorial as finished for the current user.
     * Call this when the user completes or skips the onboarding flow.
     */
    @PatchMapping("/tutorial")
    public ResponseEntity<UserResponseDto> finishTutorial(
            Authentication auth) {

        User user = userService.finishTutorial(auth.getName());
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * POST /api/users/face-model
     * Uploads a face/body photo for the current user.
     * Send as multipart/form-data with a field named "file".
     */
    @PostMapping("/face-model")
    public ResponseEntity<UserResponseDto> uploadFaceModel(
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {

        User user = userService.uploadFaceModel(auth.getName(), file);
        return ResponseEntity.ok(toDto(user));
    }

    /** Converts a User entity into a safe response object (no password). */
    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .faceModelUrl(user.getFaceModelUrl())
                .finishedTutorial(user.isFinishedTutorial())
                .build();
    }
}
