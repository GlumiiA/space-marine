package ru.itmo.is.space_marine_backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChapterResponseDTO {
    private String name;
    private Integer marinesCount;

    public ChapterResponseDTO(String name, Integer marinesCount) {
        this.name = name;
        this.marinesCount = marinesCount;
    }
}