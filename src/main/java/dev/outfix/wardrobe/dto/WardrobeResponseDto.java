package dev.outfix.wardrobe.dto;

import java.time.LocalDateTime;

import dev.outfix.clothing.dto.ClothingResponseDto;
import dev.outfix.wardrobe.entity.WardrobeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeResponseDto {

    private Long id;
    private Long scheduleId;

    private ClothingResponseDto topClothing;
    private ClothingResponseDto bottomClothing;
    private ClothingResponseDto shoesClothing;

    private String mockupJpgUrl;
    private String mockupPngUrl;

    private WardrobeStatus status;
    private boolean favorite;
    private LocalDateTime createdAt;
}
