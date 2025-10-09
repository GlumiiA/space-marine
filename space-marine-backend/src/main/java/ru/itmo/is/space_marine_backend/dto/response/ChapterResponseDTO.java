package ru.itmo.is.space_marine_backend.dto.response;

import jakarta.validation.constraints.NotBlank;


public record ChapterResponseDTO(
        @NotBlank String name,
        String parentLegion,
        String world,
        Integer marinesCount
) {}
