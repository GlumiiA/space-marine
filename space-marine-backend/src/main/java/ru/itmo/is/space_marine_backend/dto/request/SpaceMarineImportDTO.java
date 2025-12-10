package ru.itmo.is.space_marine_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;

public record SpaceMarineImportDTO(
    @NotBlank(message = "Name is required") String name,
    CoordinatesCreateDTO coordinates,
    long chapterId,
    ChapterCreateDTO chapter,
    @Positive
    double health,
    @JsonProperty("loyal") boolean isLoyal,
    @NotBlank(message = "Achievements is required") String achievements,
    AstartesCategory category
){}
