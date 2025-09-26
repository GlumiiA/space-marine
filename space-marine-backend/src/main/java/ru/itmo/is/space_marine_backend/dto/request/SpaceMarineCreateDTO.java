package ru.itmo.is.space_marine_backend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;

@Data
public class SpaceMarineCreateDTO {
    @NotBlank
    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Coordinates are required")
    private CoordinatesCreateDTO coordinates;

    @NotNull(message = "Chapter ID is required")
    private Long chapterId;

    @Positive
    private double health;

    @NotNull(message = "Loyalty is required")
    private Boolean loyal;

    @NotNull(message = "Achievements is required")
    private String achievements;

    @NotNull(message = "Category is required")
    private AstartesCategory category;

}
