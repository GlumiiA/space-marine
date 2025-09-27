package ru.itmo.is.space_marine_backend.service.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.*;
import ru.itmo.is.space_marine_backend.entity.*;
import ru.itmo.is.space_marine_backend.exception.EntityNotFoundException;
import ru.itmo.is.space_marine_backend.repository.ChapterRepository;
import ru.itmo.is.space_marine_backend.repository.CoordinatesRepository;
import ru.itmo.is.space_marine_backend.repository.SpaceMarineRepository;
import ru.itmo.is.space_marine_backend.repository.UserRepository;
import ru.itmo.is.space_marine_backend.service.SpaceMarineService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpaceMarineServiceImpl implements SpaceMarineService {

    private final SpaceMarineRepository spaceMarineRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    public SpaceMarineServiceImpl(SpaceMarineRepository spaceMarineRepository,
                                  CoordinatesRepository coordinatesRepository,
                                  ChapterRepository chapterRepository,
                                  UserRepository userRepository) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
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
            dto.setCoordinates(new CoordinatesResponseDTO(
                    marine.getCoordinates().getX(),
                    marine.getCoordinates().getY()
            ));
        }

        // маппинг Chapter
        if (marine.getChapter() != null) {
            dto.setChapter(new ChapterResponseDTO(
                    marine.getChapter().getName(),
                    marine.getChapter().getParentLegion(),
                    marine.getChapter().getWorld(),
                    marine.getChapter().getMarinesCount()
            ));
        }

        // маппинг Owner
        if (marine.getOwner() != null) {
            dto.setOwner(new UserResponseDTO(
                    marine.getOwner().getId(),
                    marine.getOwner().getUsername()
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
    public SpaceMarineResponseDTO createSpaceMarine(SpaceMarineCreateDTO dto, String username) {
        Coordinates coordinates = new Coordinates(dto.getCoordinates().getX(), dto.getCoordinates().getY());
        coordinatesRepository.save(coordinates);

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
        marine.setOwner(currentUser);
        spaceMarineRepository.save(marine);
        return mapToDto(marine);
    }

    @Override
    public SpaceMarineResponseDTO updateSpaceMarine(Long id, SpaceMarineUpdateDTO dto, String username) {
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!marine.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this SpaceMarine!");
        }
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
    public void deleteSpaceMarine(Long id, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));

        if (!marine.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not the owner of this SpaceMarine!");
        }

        spaceMarineRepository.delete(marine);
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

    @Override
    public Page<SpaceMarineResponseDTO> getAllSpaceMarines(Pageable pageable) {
        return spaceMarineRepository.findAll(pageable)
                .map(this::mapToDto); // здесь мапим каждую сущность в DTO
    }
}
