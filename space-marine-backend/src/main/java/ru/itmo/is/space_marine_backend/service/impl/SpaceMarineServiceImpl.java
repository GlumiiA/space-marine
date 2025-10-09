package ru.itmo.is.space_marine_backend.service.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.*;
import ru.itmo.is.space_marine_backend.entity.*;
import ru.itmo.is.space_marine_backend.exception.EntityNotFoundException;
import ru.itmo.is.space_marine_backend.filter.MarineFilterStrategyFactory;
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
    private final MarineFilterStrategyFactory strategyFactory;


    public SpaceMarineServiceImpl(SpaceMarineRepository spaceMarineRepository,
                                  CoordinatesRepository coordinatesRepository,
                                  ChapterRepository chapterRepository,
                                  UserRepository userRepository,
                                  MarineFilterStrategyFactory strategyFactory) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.strategyFactory = strategyFactory;
    }

    private SpaceMarineResponseDTO mapToDto(SpaceMarine marine) {
        CoordinatesResponseDTO coords = marine.getCoordinates() != null
                ? new CoordinatesResponseDTO(marine.getCoordinates().getX(), marine.getCoordinates().getY())
                : null;

        ChapterResponseDTO chapter = marine.getChapter() != null
                ? new ChapterResponseDTO(
                marine.getChapter().getName(),
                marine.getChapter().getParentLegion(),
                marine.getChapter().getWorld(),
                marine.getChapter().getMarinesCount()
        )
                : null;

        UserResponseDTO owner = marine.getOwner() != null
                ? new UserResponseDTO(
                marine.getOwner().getId(),
                marine.getOwner().getUsername()
        )
                : null;

        return new SpaceMarineResponseDTO(
                marine.getId(),
                marine.getName(),
                coords,
                marine.getCreationDate(),
                marine.getHealth(),
                marine.isLoyal(),
                marine.getAchievements(),
                marine.getCategory(),
                chapter,
                owner
        );
    }

    @Override
    public SpaceMarineResponseDTO getSpaceMarineById(Long id) {
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));
        return mapToDto(marine);
    }

    @Override
    public SpaceMarineResponseDTO createSpaceMarine(SpaceMarineCreateDTO dto, String username) {
        Coordinates coordinates = new Coordinates(dto.coordinates().x(), dto.coordinates().y());
        coordinatesRepository.save(coordinates);

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Chapter chapter = chapterRepository.findById(dto.chapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + dto.chapterId()));

        SpaceMarine marine = new SpaceMarine();
        marine.setName(dto.name());
        marine.setCoordinates(coordinates);
        marine.setChapter(chapter);
        marine.setHealth(dto.health());
        marine.setLoyal(dto.isLoyal());
        marine.setAchievements(dto.achievements());
        marine.setCategory(dto.category());
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
        coordinates.setX(dto.coordinates().x());
        coordinates.setY(dto.coordinates().y());
        coordinatesRepository.save(coordinates);

        Chapter chapter = chapterRepository.findById(dto.chapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + dto.chapterId()));

        marine.setName(dto.name());
        marine.setCoordinates(coordinates);
        marine.setChapter(chapter);
        marine.setHealth(dto.health());
        marine.setLoyal(dto.isLoyal());
        marine.setAchievements(dto.achievements());
        marine.setCategory(dto.category());

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
    public List<SpaceMarineResponseDTO> findByIsLoyal(Boolean loyal) {
        return spaceMarineRepository.findByIsLoyal(loyal).stream()
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
                .map(this::mapToDto);
    }

    @Override
    public SpaceMarineResponseDTO assignMarineToChapter(Long marineId, Long chapterId, String username) {
        SpaceMarine marine = spaceMarineRepository.findById(marineId)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + marineId));

        if (!marine.getOwner().getUsername().equals(username)) {
            throw new SecurityException("You are not the owner of this SpaceMarine!");
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + chapterId));

        marine.setChapter(chapter);
        spaceMarineRepository.save(marine);

        return mapToDto(marine);

    }

    @Override
    public Page<SpaceMarineResponseDTO> getAllFilteredAndSorted(
            int page, int size, String sortBy, String sortDir,
            String filterField, String filterValue
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<SpaceMarine> spec = null;
        if (filterField != null && filterValue != null) {
            spec = strategyFactory.getSpecification(filterField, filterValue);
        }

        Page<SpaceMarine> marines = (spec != null)
                ? spaceMarineRepository.findAll(spec, pageable)
                : spaceMarineRepository.findAll(pageable);

        return marines.map(this::mapToDto);
    }
}
