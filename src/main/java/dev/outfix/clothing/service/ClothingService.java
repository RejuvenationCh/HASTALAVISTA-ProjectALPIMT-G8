package dev.outfix.clothing.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import dev.outfix.clothing.entity.Clothing;
import dev.outfix.clothing.repository.ClothingRepository;
import dev.outfix.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClothingService {

    private final ClothingRepository clothingRepository;

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    public Clothing create(User owner, MultipartFile photoFile,
            int tokenFormalitas, String tags) throws IOException {

        String clothingImageUrl = null;
        if (photoFile != null && !photoFile.isEmpty()) {
            clothingImageUrl = savePhoto(owner.getId(), photoFile);
        }

        return clothingRepository.save(Clothing.builder()
                .user(owner)
                .clothingImageUrl(clothingImageUrl)
                .tokenFormalitas(tokenFormalitas)
                .tags(tags)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public List<Clothing> getAllByUser(User user) {
        return clothingRepository.findByUser(user);
    }

    public Clothing getById(Long id, User requestingUser) {
        Clothing item = clothingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Clothing item not found"));

        if (!item.getUser().getId().equals(requestingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return item;
    }

    public Clothing update(Long id, User requestingUser,
            Integer newToken, String newTags) {

        Clothing item = getById(id, requestingUser);
        if (newToken != null) item.setTokenFormalitas(newToken);
        if (newTags  != null) item.setTags(newTags);
        return clothingRepository.save(item);
    }

    public void delete(Long id, User requestingUser) {
        clothingRepository.delete(getById(id, requestingUser));
    }

    private String savePhoto(Long userId, MultipartFile file) throws IOException {
        Path folder = Paths.get(uploadDirectory, "clothing", userId.toString());
        Files.createDirectories(folder);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        file.transferTo(folder.resolve(filename));
        return "/uploads/clothing/" + userId + "/" + filename;
    }
}
