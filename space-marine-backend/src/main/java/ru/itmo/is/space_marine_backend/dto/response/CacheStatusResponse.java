package ru.itmo.is.space_marine_backend.dto.response;


public record CacheStatusResponse(
        boolean l2CacheEnabled,
        boolean loggingEnabled,
        Long hits,
        Long misses,
        Long puts) {
}
