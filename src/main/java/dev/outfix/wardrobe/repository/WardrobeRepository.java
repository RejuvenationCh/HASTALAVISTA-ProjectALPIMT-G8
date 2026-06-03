package dev.outfix.wardrobe.repository;

import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardrobeRepository
        extends JpaRepository<Wardrobe, Long> {

    List<Wardrobe> findByUser(
            User user);
}