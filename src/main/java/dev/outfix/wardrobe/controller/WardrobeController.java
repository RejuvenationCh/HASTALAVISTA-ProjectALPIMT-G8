package dev.outfix.wardrobe.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.outfix.user.entity.User;
import dev.outfix.user.service.UserService;
import dev.outfix.wardrobe.dto.WardrobeResponseDto;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.service.WardrobeService;
import lombok.RequiredArgsConstructor;

/**
 * Handles HTTP requests for wardrobe (clothing item) management.
 * All endpoints require a valid JWT token.
 * Users can only see and modify their own wardrobe items.
 */
@RestController
@RequestMapping("/api/wardrobes")
@RequiredArgsConstructor
public class WardrobeController {

    private final WardrobeService wardrobeService;
    private final UserService userService;

    /**
     * GET /api/wardrobes
     * Returns all clothing items belonging to the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<WardrobeResponseDto>> getAllItems(
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        List<WardrobeResponseDto> items = wardrobeService
                .getAllByUser(currentUser).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    /**
     * GET /api/wardrobes/{id}
     * Returns a single clothing item by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WardrobeResponseDto> getItemById(
            @PathVariable Long id,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        Wardrobe item = wardrobeService.getById(id, currentUser);
        return ResponseEntity.ok(toDto(item));
    }

    /**
     * POST /api/wardrobes
     * Adds a new clothing item. Send as multipart/form-data.
     * Fields: file (optional image), tokenFormalitas (int), tags (string)
     */
    @PostMapping
    public ResponseEntity<WardrobeResponseDto> addItem(
            @RequestParam(value = "file", required = false) MultipartFile photoFile,
            @RequestParam int tokenFormalitas,
            @RequestParam String tags,
            Authentication auth) throws IOException {

        User currentUser = userService.getByEmail(auth.getName());
        Wardrobe newItem = wardrobeService.create(
                currentUser, photoFile, tokenFormalitas, tags);
        return ResponseEntity.ok(toDto(newItem));
    }

    /**
     * PUT /api/wardrobes/{id}
     * Updates an existing clothing item's formality or tags.
     * Only include the fields you want to change.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WardrobeResponseDto> updateItem(
            @PathVariable Long id,
            @RequestParam(required = false) Integer tokenFormalitas,
            @RequestParam(required = false) String tags,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        Wardrobe updatedItem = wardrobeService.update(
                id, currentUser, tokenFormalitas, tags);
        return ResponseEntity.ok(toDto(updatedItem));
    }

    /**
     * DELETE /api/wardrobes/{id}
     * Removes a clothing item from the user's wardrobe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        wardrobeService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** Converts a Wardrobe entity into its response DTO. */
    private WardrobeResponseDto toDto(Wardrobe item) {
        return WardrobeResponseDto.builder()
                .id(item.getId())
                .clothingImageUrl(item.getClothingImageUrl())
                .tokenFormalitas(item.getTokenFormalitas())
                .tags(item.getTags())
                .build();
    }
}
