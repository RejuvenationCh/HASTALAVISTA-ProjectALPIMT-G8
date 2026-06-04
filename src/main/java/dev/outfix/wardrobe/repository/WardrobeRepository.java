package dev.outfix.wardrobe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;

/**
 * Handles all database queries for the Wardrobe entity.
 * Spring Data JPA automatically generates the SQL behind these methods.
 */
public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {

    /** Get all clothing items belonging to a specific user. */
    List<Wardrobe> findByUser(User user);
}
