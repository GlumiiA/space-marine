package ru.itmo.is.space_marine_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("loyal")
    private Boolean isLoyal;
    private String achievements;
    private AstartesCategory category;
    private ChapterResponseDTO chapter;
    private UserResponseDTO owner;
}