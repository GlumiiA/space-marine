package ru.itmo.is.space_marine_backend.service.impl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.SpaceMarineResponseDTO;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.entity.Coordinates;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;
import ru.itmo.is.space_marine_backend.exception.EntityNotFoundException;
import ru.itmo.is.space_marine_backend.repository.ChapterRepository;
import ru.itmo.is.space_marine_backend.repository.CoordinatesRepository;
import ru.itmo.is.space_marine_backend.repository.SpaceMarineRepository;
import ru.itmo.is.space_marine_backend.service.SpaceMarineService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpaceMarineServiceImpl implements SpaceMarineService {

    private final SpaceMarineRepository spaceMarineRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final ChapterRepository chapterRepository;

    public SpaceMarineServiceImpl(SpaceMarineRepository spaceMarineRepository,
                                  CoordinatesRepository coordinatesRepository,
                                  ChapterRepository chapterRepository) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.chapterRepository = chapterRepository;
    }

    private SpaceMarineResponseDTO mapToDto(SpaceMarine marine) {
        SpaceMarineResponseDTO dto = new SpaceMarineResponseDTO();
        dto.setId(marine.getId());
        dto.setName(marine.getName());
        dto.setCreationDate(marine.getCreationDate());
        dto.setHealth(marine.getHealth());
        dto.setLoyal(marine.getLoyal());
        dto.setAchievements(marine.getAchievements());
        dto.setCategory(marine.getCategory());

        // маппинг Coordinates
        if (marine.getCoordinates() != null) {
            dto.setCoordinates(new ru.itmo.is.space_marine_backend.dto.response.CoordinatesResponseDTO(
                    marine.getCoordinates().getX(),
                    marine.getCoordinates().getY()
            ));
        }

        // маппинг Chapter
        if (marine.getChapter() != null) {
            dto.setChapter(new ru.itmo.is.space_marine_backend.dto.response.ChapterResponseDTO(
                    marine.getChapter().getName(),
                    marine.getChapter().getMarinesCount()
            ));
        }

        return dto;
    }

    @Override
    public SpaceMarineResponseDTO getSpaceMarineById(Long id) {
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));
        return mapToDto(marine);
    }

    @Override
    public SpaceMarineResponseDTO createSpaceMarine(SpaceMarineCreateDTO dto) {
        Coordinates coordinates = new Coordinates(dto.getCoordinates().getX(), dto.getCoordinates().getY());
        coordinatesRepository.save(coordinates);

        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + dto.getChapterId()));

        SpaceMarine marine = new SpaceMarine();
        marine.setName(dto.getName());
        marine.setCoordinates(coordinates);
        marine.setChapter(chapter);
        marine.setHealth(dto.getHealth());
        marine.setLoyal(dto.getLoyal());
        marine.setAchievements(dto.getAchievements());
        marine.setCategory(dto.getCategory());

        spaceMarineRepository.save(marine);
        return mapToDto(marine);
    }

    @Override
    public SpaceMarineResponseDTO updateSpaceMarine(Long id, SpaceMarineUpdateDTO dto) {
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));

        // обновляем координаты
        Coordinates coordinates = marine.getCoordinates();
        coordinates.setX(dto.getCoordinates().getX());
        coordinates.setY(dto.getCoordinates().getY());
        coordinatesRepository.save(coordinates);

        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + dto.getChapterId()));

        marine.setName(dto.getName());
        marine.setCoordinates(coordinates);
        marine.setChapter(chapter);
        marine.setHealth(dto.getHealth());
        marine.setLoyal(dto.getLoyal());
        marine.setAchievements(dto.getAchievements());
        marine.setCategory(dto.getCategory());

        spaceMarineRepository.save(marine);
        return mapToDto(marine);
    }

    @Override
    public void deleteSpaceMarine(Long id) {
        if (!spaceMarineRepository.existsById(id)) {
            throw new EntityNotFoundException("SpaceMarine not found with id " + id);
        }
        spaceMarineRepository.deleteById(id);
    }

    @Override
    public Double calculateTotalHealth() {
        return spaceMarineRepository.calculateTotalHealth();
    }

    @Override
    public Double calculateAverageHealth() {
        return spaceMarineRepository.calculateAverageHealth();
    }

    @Override
    public SpaceMarineResponseDTO findMarineWithMinCoordinates() {
        return spaceMarineRepository.findMarineWithMinCoordinates()
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("No marines found"));
    }

    @Override
    public List<SpaceMarineResponseDTO> findByCategory(AstartesCategory category) {
        return spaceMarineRepository.findByCategory(category).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceMarineResponseDTO> findByHealthGreaterThanEqual(Double health) {
        return spaceMarineRepository.findByHealthGreaterThanEqual(health).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceMarineResponseDTO> findByLoyal(Boolean loyal) {
        return spaceMarineRepository.findByLoyal(loyal).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceMarineResponseDTO> findByChapterName(String chapterName) {
        return spaceMarineRepository.findByChapterNameContainingIgnoreCase(chapterName).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
