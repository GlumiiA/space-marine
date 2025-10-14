package ru.itmo.is.space_marine_backend.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.dto.response.CoordinatesResponseDTO;
import ru.itmo.is.space_marine_backend.dto.response.MinCoordinatesDTO;
import ru.itmo.is.space_marine_backend.repository.SpaceMarineRepository;
import ru.itmo.is.space_marine_backend.service.SpaceMarineOpsService;


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
    public MinCoordinatesDTO getMinCoordinates() {
        Object[] row = (Object[]) repository.findMinCoordinates();
        return new MinCoordinatesDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                new CoordinatesResponseDTO(
                        ((Number) row[2]).floatValue(),
                        ((Number) row[3]).floatValue()
                )
        );
    }
}
