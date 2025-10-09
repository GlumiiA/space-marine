package ru.itmo.is.space_marine_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;

import java.time.LocalDateTime;

public record SpaceMarineResponseDTO(
        Long id,
        @NotBlank String name,
        CoordinatesResponseDTO coordinates,
        LocalDateTime creationDate,
        Double health,
        @JsonProperty("loyal") Boolean isLoyal,
        @NotBlank String achievements,
        AstartesCategory category,
        ChapterResponseDTO chapter,
        UserResponseDTO owner
) {}