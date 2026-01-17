package ru.itmo.is.space_marine_backend.dto.response;

import java.util.Optional;

public record CacheStatusResponse(
        boolean l2CacheEnabled,
        boolean loggingEnabled,
        Optional<CacheStats> stats) {
}
