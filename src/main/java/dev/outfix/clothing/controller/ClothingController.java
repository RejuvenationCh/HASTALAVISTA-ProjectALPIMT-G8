package dev.outfix.clothing.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.outfix.clothing.dto.ClothingResponseDto;
import dev.outfix.clothing.entity.Clothing;
import dev.outfix.clothing.service.ClothingService;
import dev.outfix.user.entity.User;
import dev.outfix.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clothing")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothingService clothingService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ClothingResponseDto>> getAll(Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        List<ClothingResponseDto> items = clothingService.getAllByUser(user)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClothingResponseDto> getById(
            @PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(clothingService.getById(id, user)));
    }

    @PostMapping
    public ResponseEntity<ClothingResponseDto> create(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam int tokenFormalitas,
            @RequestParam String tags,
            Authentication auth) throws IOException {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(
                clothingService.create(user, file, tokenFormalitas, tags)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClothingResponseDto> update(
            @PathVariable Long id,
            @RequestParam(required = false) Integer tokenFormalitas,
            @RequestParam(required = false) String tags,
            Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(
                clothingService.update(id, user, tokenFormalitas, tags)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        clothingService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ClothingResponseDto> toggleFavorite(
            @PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(clothingService.toggleFavorite(id, user)));
    }

    private ClothingResponseDto toDto(Clothing c) {
        return ClothingResponseDto.builder()
                .id(c.getId())
                .clothingImageUrl(c.getClothingImageUrl())
                .tokenFormalitas(c.getTokenFormalitas())
                .tags(c.getTags())
                .favorite(c.isFavorite())
                .build();
    }
}
