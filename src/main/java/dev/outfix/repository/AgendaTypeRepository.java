package dev.outfix.repository;

import dev.outfix.entity.AgendaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgendaTypeRepository extends JpaRepository<AgendaType, Long> {

    Optional<AgendaType> findByName(String name);

}