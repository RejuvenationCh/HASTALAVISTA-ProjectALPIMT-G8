package dev.outfix.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.outfix.schedule.entity.Schedule;
import dev.outfix.user.entity.User;

public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUser(User user);
}
