package dev.outfix.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.outfix.clothing.dto.ClothingResponseDto;
import dev.outfix.clothing.entity.Clothing;
import dev.outfix.clothing.repository.ClothingRepository;
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
}
