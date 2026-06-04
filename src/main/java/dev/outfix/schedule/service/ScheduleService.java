package dev.outfix.schedule.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.outfix.schedule.dto.CreateScheduleRequestDto;
import dev.outfix.schedule.dto.UpdateScheduleRequestDto;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.schedule.repository.ScheduleRepository;
import dev.outfix.user.entity.User;
import lombok.RequiredArgsConstructor;

/**
 * Handles all business logic for schedule management.
 * Ownership is enforced — users can only access their own schedules.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    /** Creates and saves a new scheduled activity for the given user. */
    public Schedule create(User owner, CreateScheduleRequestDto request) {
        Schedule newSchedule = Schedule.builder()
                .user(owner)
                .activityName(request.getActivityName())
                .targetToken(request.getTargetToken())
                .targetTag(request.getTargetTag())
                .dresscode(request.getDresscode())
                .build();
        return scheduleRepository.save(newSchedule);
    }

    /** Returns all scheduled activities belonging to the given user. */
    public List<Schedule> getAllByUser(User user) {
        return scheduleRepository.findByUser(user);
    }

    /**
     * Finds a single schedule by its ID.
     * Throws 404 if not found, 403 if it belongs to a different user.
     */
    public Schedule getById(Long scheduleId, User requestingUser) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Schedule not found"));

        boolean notTheOwner = !schedule.getUser().getId()
                .equals(requestingUser.getId());
        if (notTheOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return schedule;
    }

    /**
     * Updates a schedule's fields. Only non-null fields are changed.
     * This avoids overwriting data the client did not intend to update.
     */
    public Schedule update(Long scheduleId, User requestingUser,
            UpdateScheduleRequestDto request) {

        Schedule schedule = getById(scheduleId, requestingUser);

        if (request.getActivityName() != null) {
            schedule.setActivityName(request.getActivityName());
        }
        if (request.getTargetToken() != null) {
            schedule.setTargetToken(request.getTargetToken());
        }
        if (request.getTargetTag() != null) {
            schedule.setTargetTag(request.getTargetTag());
        }
        if (request.getDresscode() != null) {
            // empty string is treated as "clear the dresscode"
            schedule.setDresscode(request.getDresscode().isBlank() ? null : request.getDresscode());
        }

        return scheduleRepository.save(schedule);
    }

    /** Deletes a schedule. Only the owner can delete their own schedules. */
    public void delete(Long scheduleId, User requestingUser) {
        Schedule schedule = getById(scheduleId, requestingUser);
        scheduleRepository.delete(schedule);
    }
}
