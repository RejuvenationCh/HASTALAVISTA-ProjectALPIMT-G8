package dev.outfix.user.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.outfix.user.entity.User;
import dev.outfix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Handles user profile operations such as tutorial completion and face model upload.
 * Authentication (login/register) is handled separately in AuthService.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Base directory where all uploaded files are stored. */
    @Value("${app.upload.dir}")
    private String uploadDirectory;

    /** Finds a user by email. Used by controllers to get the current user. */
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    /**
     * Marks the user's tutorial as finished.
     * Called when the user completes or skips onboarding.
     */
    public User finishTutorial(String email) {
        User user = getByEmail(email);
        user.setFinishedTutorial(true);
        return userRepository.save(user);
    }

    /**
     * Saves the uploaded face/body photo to the server and stores its URL.
     * Files are saved to: uploads/face-models/{userId}/{uuid_filename}
     */
    public User uploadFaceModel(String email, MultipartFile photoFile)
            throws IOException {

        User user = getByEmail(email);

        // Create the folder for this user if it doesn't exist
        Path userFaceModelFolder = Paths.get(
                uploadDirectory, "face-models", user.getId().toString());
        Files.createDirectories(userFaceModelFolder);

        // Use a UUID prefix to avoid filename collisions
        String uniqueFilename = UUID.randomUUID() + "_" + photoFile.getOriginalFilename();
        Path savedFilePath = userFaceModelFolder.resolve(uniqueFilename);
        photoFile.transferTo(savedFilePath);

        // Store the public-facing URL so the frontend can display the image
        String publicUrl = "/uploads/face-models/" + user.getId() + "/" + uniqueFilename;
        user.setFaceModelUrl(publicUrl);

        return userRepository.save(user);
    }
}
