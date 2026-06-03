package dev.outfix.repository;

import dev.outfix.entity.OutfitRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutfitRecommendationRepository extends JpaRepository<OutfitRecommendation, Long> {

    List<OutfitRecommendation> findByUserId(Long userId);

    List<OutfitRecommendation> findByAgendaId(Long agendaId);

}