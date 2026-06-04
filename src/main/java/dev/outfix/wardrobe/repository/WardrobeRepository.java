package dev.outfix.wardrobe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;

/**
 * Handles all database queries for the Wardrobe entity.
 * Spring Data JPA automatically generates the SQL behind these methods.
 */
public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {

    /** Get all clothing items belonging to a specific user. */
    List<Wardrobe> findByUser(User user);

    /**
     * Recommendation query.
     * - token: uses >= so items at or above the required formality level qualify.
     * - tag: clothing category slot filter (e.g. "Top", "Bottom").
     * - dresscode: optional style requirement (e.g. "Batik", "Sportswear").
     *   Pass null to skip dresscode filtering.
     */
    @Query("SELECT w FROM Wardrobe w WHERE w.user = :user " +
           "AND w.tokenFormalitas >= :token " +
           "AND w.tags LIKE %:tag% " +
           "AND (:dresscode IS NULL OR w.tags LIKE %:dresscode%)")
    List<Wardrobe> findMatchingItems(@Param("user") User user,
                                    @Param("token") int token,
                                    @Param("tag") String tag,
                                    @Param("dresscode") String dresscode);
}
