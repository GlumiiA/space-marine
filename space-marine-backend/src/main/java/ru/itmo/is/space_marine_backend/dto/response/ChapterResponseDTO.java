package ru.itmo.is.space_marine_backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChapterResponseDTO {
    private String name;
    private Integer marinesCount;
    private String parentLegion;

    private String world;

    public ChapterResponseDTO(String name, String parentLegion, String world, Integer marinesCount) {
        this.name = name;
        this.parentLegion = parentLegion;
        this.world = world;
        this.marinesCount = marinesCount;
    }
}