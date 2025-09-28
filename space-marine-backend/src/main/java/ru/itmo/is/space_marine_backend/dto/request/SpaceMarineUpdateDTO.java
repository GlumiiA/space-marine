package ru.itmo.is.space_marine_backend.dto.request;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;


@Data
public class SpaceMarineUpdateDTO {
    @NotBlank(message = "Name cannot be blank")
    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Coordinates are required")
    private CoordinatesUpdateDTO coordinates;

    @NotNull(message = "Chapter ID is required")
    private Long chapterId;

    @Positive(message = "Health must be positive")
    @NotNull(message = "Health is required")
    private Double health;

    @NotNull(message = "Achievements is required")
    private Boolean loyal;

    @NotNull(message = "Achievements is required")
    private String achievements;

    @NotNull(message = "Category is required")
    private AstartesCategory category;
}