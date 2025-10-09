package ru.itmo.is.space_marine_backend.dto.request;
import jakarta.validation.constraints.NotBlank;


public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
