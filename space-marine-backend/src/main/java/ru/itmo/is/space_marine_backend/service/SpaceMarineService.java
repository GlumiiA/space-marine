package ru.itmo.is.space_marine_backend.service;

import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.SpaceMarineResponseDTO;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;

import java.util.List;

public interface SpaceMarineService {
    SpaceMarineResponseDTO getSpaceMarineById(Long id);

    SpaceMarineResponseDTO createSpaceMarine(SpaceMarineCreateDTO dto);

    SpaceMarineResponseDTO updateSpaceMarine(Long id, SpaceMarineUpdateDTO dto);

    void deleteSpaceMarine(Long id);

    Double calculateTotalHealth();

    Double calculateAverageHealth();

    SpaceMarineResponseDTO findMarineWithMinCoordinates();

    List<SpaceMarineResponseDTO> findByCategory(AstartesCategory category);

    List<SpaceMarineResponseDTO> findByHealthGreaterThanEqual(Double health);

    List<SpaceMarineResponseDTO> findByLoyal(Boolean loyal);

    List<SpaceMarineResponseDTO> findByChapterName(String chapterName);
}