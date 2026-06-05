package dev.outfix.wardrobe.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.outfix.clothing.entity.Clothing;
import dev.outfix.clothing.service.ClothingService;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.schedule.service.ScheduleService;
import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.dto.CreateWardrobeRequestDto;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.entity.WardrobeStatus;
import dev.outfix.wardrobe.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardrobeService {

    private final WardrobeRepository wardrobeRepository;
    private final ClothingService clothingService;
    private final ScheduleService scheduleService;
    private final WardrobeMockupGenerator mockupGenerator;

    /**
     * Saves the outfit immediately with PENDING status, then fires async ComfyUI
     * generation if the user has a face model and all three clothing slots are filled.
     */
    public Wardrobe create(User owner, CreateWardrobeRequestDto request) {
        Schedule schedule = resolveSchedule(request.getScheduleId(), owner);
        Clothing top    = resolveClothing(request.getTopClothingId(), owner);
        Clothing bottom = resolveClothing(request.getBottomClothingId(), owner);
        Clothing shoes  = resolveClothing(request.getShoesClothingId(), owner);

        Wardrobe wardrobe = wardrobeRepository.save(Wardrobe.builder()
                .user(owner)
                .schedule(schedule)
                .topClothing(top)
                .bottomClothing(bottom)
                .shoesClothing(shoes)
                .status(WardrobeStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        // Top + pants only — shoes are optional and ignored by generation.
        boolean canGenerate = owner.getFaceModelUrl() != null
                && top != null && bottom != null;

        if (canGenerate) {
            mockupGenerator.generate(wardrobe.getId(), owner);
        }

        return wardrobe;
    }

    public List<Wardrobe> getAllByUser(User user) {
        return wardrobeRepository.findByUser(user);
    }

    public Wardrobe getById(Long id, User requestingUser) {
        Wardrobe w = wardrobeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wardrobe not found"));
        if (!w.getUser().getId().equals(requestingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return w;
    }

    public void delete(Long id, User requestingUser) {
        wardrobeRepository.delete(getById(id, requestingUser));
    }

    public Wardrobe toggleFavorite(Long id, User requestingUser) {
        Wardrobe w = getById(id, requestingUser);
        w.setFavorite(!w.isFavorite());
        return wardrobeRepository.save(w);
    }

    private Schedule resolveSchedule(Long scheduleId, User owner) {
        if (scheduleId == null) return null;
        return scheduleService.getById(scheduleId, owner);
    }

    private Clothing resolveClothing(Long clothingId, User owner) {
        if (clothingId == null) return null;
        return clothingService.getById(clothingId, owner);
    }
}
