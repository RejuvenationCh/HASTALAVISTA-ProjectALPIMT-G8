package dev.outfix.wardrobe.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;

/**
 * Handles all business logic for wardrobe (clothing item) management.
 * Ownership is enforced — users can only access their own items.
 */
@Service
@RequiredArgsConstructor
public class WardrobeService {

    private final WardrobeRepository wardrobeRepository;

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    /**
     * Adds a new clothing item to the user's wardrobe.
     * Optionally saves an uploaded photo and stores its public URL.
     */
    public Wardrobe create(
            User owner,
            MultipartFile photoFile,
            int tokenFormalitas,
            String tags) throws IOException {

        String clothingImageUrl = null;

        if (photoFile != null && !photoFile.isEmpty()) {
            clothingImageUrl = saveClothingPhoto(owner.getId(), photoFile);
        }

        Wardrobe newItem = Wardrobe.builder()
                .user(owner)
                .clothingImageUrl(clothingImageUrl)
                .tokenFormalitas(tokenFormalitas)
                .tags(tags)
                .createdAt(LocalDateTime.now())
                .build();

        return wardrobeRepository.save(newItem);
    }

    /** Returns all clothing items belonging to the given user. */
    public List<Wardrobe> getAllByUser(User user) {
        return wardrobeRepository.findByUser(user);
    }

    /**
     * Finds a single wardrobe item by its ID.
     * Throws 404 if not found, 403 if it belongs to a different user.
     */
    public Wardrobe getById(Long itemId, User requestingUser) {
        Wardrobe item = wardrobeRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wardrobe item not found"));

        boolean notTheOwner = !item.getUser().getId().equals(requestingUser.getId());
        if (notTheOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return item;
    }

    /**
     * Updates the formality token and/or tags of an existing item.
     * Only updates fields that are provided (non-null).
     */
    public Wardrobe update(Long itemId, User requestingUser,
            Integer newTokenFormalitas, String newTags) {

        Wardrobe item = getById(itemId, requestingUser);

        if (newTokenFormalitas != null) {
            item.setTokenFormalitas(newTokenFormalitas);
        }
        if (newTags != null) {
            item.setTags(newTags);
        }

        return wardrobeRepository.save(item);
    }

    /** Deletes a clothing item. Only the owner can delete their own items. */
    public void delete(Long itemId, User requestingUser) {
        Wardrobe item = getById(itemId, requestingUser);
        wardrobeRepository.delete(item);
    }

    /**
     * Saves a clothing photo to disk and returns its public URL.
     * Files are stored at: uploads/wardrobes/{userId}/{uuid_filename}
     */
    private String saveClothingPhoto(Long userId, MultipartFile photoFile)
            throws IOException {

        Path userFolder = Paths.get(uploadDirectory, "wardrobes", userId.toString());
        Files.createDirectories(userFolder);

        String uniqueFilename = UUID.randomUUID() + "_" + photoFile.getOriginalFilename();
        photoFile.transferTo(userFolder.resolve(uniqueFilename));

        return "/uploads/wardrobes/" + userId + "/" + uniqueFilename;
    }
}
