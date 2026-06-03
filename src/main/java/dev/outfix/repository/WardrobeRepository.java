package dev.outfix.repository;

import dev.outfix.entity.Wardrobe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {

    Optional<Wardrobe> findByUserId(Long userId);

}