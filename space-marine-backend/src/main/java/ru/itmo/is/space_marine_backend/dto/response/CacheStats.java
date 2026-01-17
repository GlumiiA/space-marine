package ru.itmo.is.space_marine_backend.dto.response;

public record CacheStats(
        Long hits,
        Long misses,
        Long puts) {
}
