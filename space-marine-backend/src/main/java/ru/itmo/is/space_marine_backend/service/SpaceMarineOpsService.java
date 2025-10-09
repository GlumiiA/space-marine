package ru.itmo.is.space_marine_backend.service;

import ru.itmo.is.space_marine_backend.dto.response.MinCoordinatesDTO;

import java.util.Map;

public interface SpaceMarineOpsService {
    Long getSumHealth();
    Double getAvgHealth();
    MinCoordinatesDTO getMinCoordinates();

}
