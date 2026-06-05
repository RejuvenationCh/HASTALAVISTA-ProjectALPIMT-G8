package dev.outfix.wardrobe.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.outfix.clothing.dto.ClothingResponseDto;
import dev.outfix.clothing.entity.Clothing;
import dev.outfix.user.entity.User;
import dev.outfix.user.service.UserService;
import dev.outfix.wardrobe.dto.CreateWardrobeRequestDto;
import dev.outfix.wardrobe.dto.WardrobeResponseDto;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.service.WardrobeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wardrobes")
@RequiredArgsConstructor
public class WardrobeController {

    private final WardrobeService wardrobeService;
    private final UserService userService;

    /** POST /api/wardrobes — save an outfit and kick off async ComfyUI generation. */
    @PostMapping
    public ResponseEntity<WardrobeResponseDto> create(
            @RequestBody CreateWardrobeRequestDto request,
            Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(wardrobeService.create(user, request)));
    }

    /** GET /api/wardrobes — all saved outfits for the logged-in user. */
    @GetMapping
    public ResponseEntity<List<WardrobeResponseDto>> getAll(Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        List<WardrobeResponseDto> list = wardrobeService.getAllByUser(user)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    /** GET /api/wardrobes/{id} — single outfit with current generation status. */
    @GetMapping("/{id}")
    public ResponseEntity<WardrobeResponseDto> getById(
            @PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(wardrobeService.getById(id, user)));
    }

    /** DELETE /api/wardrobes/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        wardrobeService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/wardrobes/{id}/favorite — toggle the favorite flag. */
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<WardrobeResponseDto> toggleFavorite(
            @PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(wardrobeService.toggleFavorite(id, user)));
    }

    private WardrobeResponseDto toDto(Wardrobe w) {
        return WardrobeResponseDto.builder()
                .id(w.getId())
                .scheduleId(w.getSchedule() != null ? w.getSchedule().getId() : null)
                .topClothing(clothingDto(w.getTopClothing()))
                .bottomClothing(clothingDto(w.getBottomClothing()))
                .shoesClothing(clothingDto(w.getShoesClothing()))
                .mockupJpgUrl(w.getMockupJpgUrl())
                .mockupPngUrl(w.getMockupPngUrl())
                .status(w.getStatus())
                .favorite(w.isFavorite())
                .createdAt(w.getCreatedAt())
                .build();
    }

    private ClothingResponseDto clothingDto(Clothing c) {
        if (c == null) return null;
        return ClothingResponseDto.builder()
                .id(c.getId())
                .clothingImageUrl(c.getClothingImageUrl())
                .tokenFormalitas(c.getTokenFormalitas())
                .tags(c.getTags())
                .favorite(c.isFavorite())
                .build();
    }
}
