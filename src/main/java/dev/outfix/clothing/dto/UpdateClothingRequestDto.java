package dev.outfix.clothing.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClothingRequestDto {

    private Integer tokenFormalitas;
    private String tags;
}
