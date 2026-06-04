package dev.outfix.wardrobe.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.outfix.user.entity.User;
import dev.outfix.wardrobe.entity.Wardrobe;
import dev.outfix.wardrobe.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardrobeService {

    private final WardrobeRepository wardrobeRepository;

    public Wardrobe createWardrobe(
            User user,
            String clothingImageUrl,
            int tokenFormalitas,
            String tags) {

        Wardrobe wardrobe = Wardrobe.builder()
                .user(user)
                .clothingImageUrl(clothingImageUrl)
                .tokenFormalitas(tokenFormalitas)
                .tags(tags)
                .createdAt(LocalDateTime.now())
                .build();

        return wardrobeRepository.save(wardrobe);
    }

    public List<Wardrobe> getUserWardrobes(User user) {
        return wardrobeRepository.findByUser(user);
    }

    public void deleteWardrobe(Long id) {
        wardrobeRepository.deleteById(id);
    }
}
