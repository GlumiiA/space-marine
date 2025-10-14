package ru.itmo.is.space_marine_backend.dto.response;

public record SpaceMarineQueryParams(
        int page,
        int size,
        String sortBy,
        String sortDir,
        String filterField,
        String filterValue
) {}