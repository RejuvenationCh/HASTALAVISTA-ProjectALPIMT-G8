package dev.outfix.wardrobe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;

public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {

    List<Wardrobe> findByUser(User user);
}
