package ru.itmo.is.space_marine_backend.dto.response;

import jakarta.validation.constraints.NotBlank;

public record AuthResponseDTO(
        @NotBlank String token
) {}
