package dev.outfix.repository;

import dev.outfix.entity.ClothingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

    List<ClothingItem> findByWardrobeId(Long wardrobeId);

    List<ClothingItem> findByCategoryId(Long categoryId);

    List<ClothingItem> findByWardrobeIdAndCategoryId(Long wardrobeId, Long categoryId);

}