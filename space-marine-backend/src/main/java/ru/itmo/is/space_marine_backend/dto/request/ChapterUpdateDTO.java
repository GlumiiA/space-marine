package ru.itmo.is.space_marine_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

public record ChapterUpdateDTO(
        @NotBlank(message = "Name is required") String name,
        String parentLegion,
        Optional<String> world
) {}