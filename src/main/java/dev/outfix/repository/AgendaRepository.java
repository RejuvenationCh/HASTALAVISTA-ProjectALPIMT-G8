package dev.outfix.repository;

import dev.outfix.entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    List<Agenda> findByUserId(Long userId);

    List<Agenda> findByUserIdOrderByDateAsc(Long userId);

    List<Agenda> findByDate(LocalDate date);

}