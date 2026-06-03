package dev.outfix.repository;

import dev.outfix.entity.ClothingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClothingCategoryRepository extends JpaRepository<ClothingCategory, Long> {

    Optional<ClothingCategory> findByName(String name);

}