package dev.outfix.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.outfix.clothing.dto.ClothingResponseDto;
import dev.outfix.clothing.entity.Clothing;
import dev.outfix.clothing.repository.ClothingRepository;
import dev.outfix.recommendation.dto.OutfitRecommendationResponseDto;
import dev.outfix.recommendation.dto.RecommendationResponseDto;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.schedule.service.ScheduleService;
import dev.outfix.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ScheduleService scheduleService;
    private final ClothingRepository clothingRepository;

    public RecommendationResponseDto recommend(Long scheduleId, User requestingUser) {
        Schedule schedule = scheduleService.getById(scheduleId, requestingUser);

        List<Clothing> matches = clothingRepository.findMatchingItems(
                requestingUser,
                schedule.getTargetToken(),
                schedule.getTargetTag(),
                schedule.getDresscode());

        if (matches.isEmpty()) {
            return RecommendationResponseDto.empty();
        }

        List<ClothingResponseDto> items = matches.stream()
                .map(c -> ClothingResponseDto.builder()
                        .id(c.getId())
                        .clothingImageUrl(c.getClothingImageUrl())
                        .tokenFormalitas(c.getTokenFormalitas())
                        .tags(c.getTags())
                        .build())
                .toList();

        return RecommendationResponseDto.found(items);
    }

    /**
     * Picks a single outfit (one top + one pair of pants) for the given schedule.
     * Each garment must meet the schedule's formality token (and dresscode, if set).
     * Reuses {@link ClothingRepository#findMatchingItems} once per category.
     */
    public OutfitRecommendationResponseDto recommendOutfit(Long scheduleId, User requestingUser) {
        Schedule schedule = scheduleService.getById(scheduleId, requestingUser);

        ClothingResponseDto top = firstMatch(requestingUser, schedule, "Top");
        ClothingResponseDto bottom = firstMatch(requestingUser, schedule, "Bottom");

        return OutfitRecommendationResponseDto.of(top, bottom);
    }

    /** Returns the first clothing item matching the schedule for the given category tag, or null. */
    private ClothingResponseDto firstMatch(User user, Schedule schedule, String categoryTag) {
        List<Clothing> matches = clothingRepository.findMatchingItems(
                user,
                schedule.getTargetToken(),
                categoryTag,
                schedule.getDresscode());

        return matches.isEmpty() ? null : toDto(matches.get(0));
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
