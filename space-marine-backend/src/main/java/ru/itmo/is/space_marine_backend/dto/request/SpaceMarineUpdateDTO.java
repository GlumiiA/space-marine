package ru.itmo.is.space_marine_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;


public record SpaceMarineUpdateDTO(
        @NotBlank(message = "Name is required") String name,
        CoordinatesUpdateDTO coordinates,
        long chapterId,
        @Positive double health,
        @JsonProperty("loyal") boolean isLoyal,
        @NotBlank(message = "Achievements is required") String achievements,
        AstartesCategory category
) {}