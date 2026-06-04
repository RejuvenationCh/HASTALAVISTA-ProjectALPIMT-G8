package dev.outfix.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.outfix.schedule.entity.Schedule;
import dev.outfix.user.entity.User;

/**
 * Handles all database queries for the Schedule entity.
 * Spring Data JPA automatically generates the SQL behind these methods.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /** Get all scheduled activities belonging to a specific user. */
    List<Schedule> findByUser(User user);
}
