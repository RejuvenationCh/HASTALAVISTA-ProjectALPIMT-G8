package dev.outfix.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.outfix.schedule.dto.CreateScheduleRequestDto;
import dev.outfix.schedule.dto.ScheduleResponseDto;
import dev.outfix.schedule.dto.UpdateScheduleRequestDto;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.schedule.service.ScheduleService;
import dev.outfix.user.entity.User;
import dev.outfix.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Handles HTTP requests for schedule management.
 * All endpoints require a valid JWT token.
 * Users can only see and modify their own schedules.
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserService userService;

    /**
     * GET /api/schedules
     * Returns all scheduled activities for the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponseDto>> getAllSchedules(
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        List<ScheduleResponseDto> schedules = scheduleService
                .getAllByUser(currentUser).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(schedules);
    }

    /**
     * GET /api/schedules/{id}
     * Returns a single schedule by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> getScheduleById(
            @PathVariable Long id,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        Schedule schedule = scheduleService.getById(id, currentUser);
        return ResponseEntity.ok(toDto(schedule));
    }

    /**
     * POST /api/schedules
     * Creates a new scheduled activity.
     */
    @PostMapping
    public ResponseEntity<ScheduleResponseDto> createSchedule(
            @Valid @RequestBody CreateScheduleRequestDto request,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        Schedule newSchedule = scheduleService.create(currentUser, request);
        return ResponseEntity.ok(toDto(newSchedule));
    }

    /**
     * PUT /api/schedules/{id}
     * Updates an existing schedule. Only include fields you want to change.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(
            @PathVariable Long id,
            @RequestBody UpdateScheduleRequestDto request,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        Schedule updatedSchedule = scheduleService.update(
                id, currentUser, request);
        return ResponseEntity.ok(toDto(updatedSchedule));
    }

    /**
     * DELETE /api/schedules/{id}
     * Deletes a scheduled activity.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long id,
            Authentication auth) {

        User currentUser = userService.getByEmail(auth.getName());
        scheduleService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** Converts a Schedule entity into its response DTO. */
    private ScheduleResponseDto toDto(Schedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .activityName(schedule.getActivityName())
                .eventDate(schedule.getEventDate())
                .targetToken(schedule.getTargetToken())
                .targetTag(schedule.getTargetTag())
                .dresscode(schedule.getDresscode())
                .build();
    }
}
