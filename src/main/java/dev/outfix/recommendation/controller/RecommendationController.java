package dev.outfix.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.outfix.recommendation.dto.OutfitOptionsResponseDto;
import dev.outfix.recommendation.dto.OutfitRecommendationResponseDto;
import dev.outfix.recommendation.dto.RecommendationResponseDto;
import dev.outfix.recommendation.service.RecommendationService;
import dev.outfix.user.entity.User;
import dev.outfix.user.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * GET /api/recommendation/{scheduleId}
 *
 * Returns wardrobe items whose formality token and tag match the given schedule.
 * If nothing matches, returns { "status": "empty", "action": "suggest_input" }.
 */
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @GetMapping("/{scheduleId}")
    public ResponseEntity<RecommendationResponseDto> recommend(
            @PathVariable Long scheduleId,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        RecommendationResponseDto result =
                recommendationService.recommend(scheduleId, currentUser);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/recommendation/{scheduleId}/outfit
     * Returns a single top + pants outfit matched to the schedule's formality token.
     */
    @GetMapping("/{scheduleId}/outfit")
    public ResponseEntity<OutfitRecommendationResponseDto> recommendOutfit(
            @PathVariable Long scheduleId,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(
                recommendationService.recommendOutfit(scheduleId, currentUser));
    }

    /**
     * GET /api/recommendation/{scheduleId}/options
     * Returns ALL matching tops and bottoms for the schedule — used by the swap picker.
     */
    @GetMapping("/{scheduleId}/options")
    public ResponseEntity<OutfitOptionsResponseDto> recommendOptions(
            @PathVariable Long scheduleId,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(
                recommendationService.recommendOutfitOptions(scheduleId, currentUser));
    }
}
