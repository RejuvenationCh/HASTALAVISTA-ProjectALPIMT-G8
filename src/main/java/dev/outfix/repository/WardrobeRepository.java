package dev.outfix.repository;

import dev.outfix.entity.Wardrobe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {

    List<Wardrobe> findByUserId(Long userId);

    @Query("SELECT w FROM Wardrobe w WHERE w.user.id = :userId AND w.tokenFormalitas = :targetToken AND w.tags LIKE %:targetTag%")
    List<Wardrobe> findMatchingItems(@Param("userId") Long userId,
                                     @Param("targetToken") Integer targetToken,
                                     @Param("targetTag") String targetTag);

}
