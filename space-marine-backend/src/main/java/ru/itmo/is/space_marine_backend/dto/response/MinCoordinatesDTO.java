package ru.itmo.is.space_marine_backend.dto.response;

public record MinCoordinatesDTO(
        Long id,
        String name,
        CoordinatesResponseDTO coordinates
) {}