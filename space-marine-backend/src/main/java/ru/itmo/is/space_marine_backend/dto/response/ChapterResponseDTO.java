package ru.itmo.is.space_marine_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponseDTO {
    private String name;
    private String parentLegion;
    private String world;
    private Integer marinesCount;
}