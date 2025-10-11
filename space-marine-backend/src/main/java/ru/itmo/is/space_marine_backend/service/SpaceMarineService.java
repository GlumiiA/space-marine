package ru.itmo.is.space_marine_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.SpaceMarineResponseDTO;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;

import java.util.List;

public interface SpaceMarineService {
    SpaceMarineResponseDTO getSpaceMarineById(Long id);

    SpaceMarineResponseDTO createSpaceMarine(SpaceMarineCreateDTO dto, String username);

    SpaceMarineResponseDTO updateSpaceMarine(Long id, SpaceMarineUpdateDTO dto, Long userId);

    void deleteSpaceMarine(Long id, Long userId);

    List<SpaceMarineResponseDTO> findByCategory(AstartesCategory category);

    List<SpaceMarineResponseDTO> findByHealthGreaterThanEqual(Double health);

    List<SpaceMarineResponseDTO> findByIsLoyal(Boolean isLoyal);

    List<SpaceMarineResponseDTO> findByChapterName(String chapterName);

    Page<SpaceMarineResponseDTO> getAllSpaceMarines(Pageable pageable);

    SpaceMarineResponseDTO assignMarineToChapter(Long marineId, Long chapterId, String username);

    Page<SpaceMarineResponseDTO> getAllFilteredAndSorted(int page, int size, String sortBy, String sortDir, String filterField, String filterValue);
}