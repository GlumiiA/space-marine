package ru.itmo.is.space_marine_backend.service;

import java.util.Map;

public interface SpaceMarineOpsService {
    Long getSumHealth();
    Double getAvgHealth();
    Map<String, Object> getMinCoordinates();

}
