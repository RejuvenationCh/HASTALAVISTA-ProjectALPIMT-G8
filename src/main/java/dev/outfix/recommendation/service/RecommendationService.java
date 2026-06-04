package dev.outfix.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.outfix.recommendation.dto.RecommendationResponseDto;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.schedule.service.ScheduleService;
import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.dto.WardrobeResponseDto;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ScheduleService scheduleService;
    private final WardrobeRepository wardrobeRepository;

    /**
     * Finds wardrobe items that match the formality token and tag of the given schedule.
     * Returns the empty fallback payload when no items match.
     */
    public RecommendationResponseDto recommend(Long scheduleId, User requestingUser) {
        Schedule schedule = scheduleService.getById(scheduleId, requestingUser);

        List<Wardrobe> matches = wardrobeRepository.findMatchingItems(
                requestingUser,
                schedule.getTargetToken(),
                schedule.getTargetTag(),
                schedule.getDresscode());

        if (matches.isEmpty()) {
            return RecommendationResponseDto.empty();
        }

        List<WardrobeResponseDto> items = matches.stream()
                .map(w -> WardrobeResponseDto.builder()
                        .id(w.getId())
                        .clothingImageUrl(w.getClothingImageUrl())
                        .tokenFormalitas(w.getTokenFormalitas())
                        .tags(w.getTags())
                        .build())
                .toList();

        return RecommendationResponseDto.found(items);
    }
}
