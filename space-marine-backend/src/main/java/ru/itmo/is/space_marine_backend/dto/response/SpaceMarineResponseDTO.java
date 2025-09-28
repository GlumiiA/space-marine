package ru.itmo.is.space_marine_backend.dto.response;

import lombok.Data;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;

import java.time.LocalDateTime;

@Data
public class SpaceMarineResponseDTO {
    private Long id;
    private String name;
    private CoordinatesResponseDTO coordinates;
    private LocalDateTime creationDate;
    private Double health;
    private Boolean loyal;
    private String achievements;
    private AstartesCategory category;
    private ChapterResponseDTO chapter;
    private UserResponseDTO owner;
}