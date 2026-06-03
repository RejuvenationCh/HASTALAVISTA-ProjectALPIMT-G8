package dev.outfix.repository;

import dev.outfix.entity.OutfitItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutfitItemRepository extends JpaRepository<OutfitItem, Long> {

    List<OutfitItem> findByRecommendationId(Long recommendationId);

}