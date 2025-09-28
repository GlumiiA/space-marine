package ru.itmo.is.space_marine_backend.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.repository.SpaceMarineRepository;
import ru.itmo.is.space_marine_backend.service.SpaceMarineOpsService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpaceMarineOpsServiceImpl implements SpaceMarineOpsService {
    private final SpaceMarineRepository repository;

    @Override
    public Long getSumHealth() {
        return repository.sumHealth();
    }

    @Override
    public Double getAvgHealth() {
        return repository.avgHealth();
    }

    @Override
    public Map<String, Object> getMinCoordinates() {
        Object[] row = (Object[]) repository.findMinCoordinates();
        Map<String, Object> result = new HashMap<>();
        result.put("id", row[0]);
        result.put("name", row[1]);
        Map<String, Object> coords = new HashMap<>();
        coords.put("x", row[2]);
        coords.put("y", row[3]);
        result.put("coordinates", coords);
        return result;
    }
}
