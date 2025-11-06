package ru.itmo.is.space_marine_backend.service.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
    private static final int MAX_PAGE_SIZE = 20;

    private final SpaceMarineRepository spaceMarineRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final MarineFilterStrategyFactory strategyFactory;
    public final JwtService jwtService;


    public SpaceMarineServiceImpl(SpaceMarineRepository spaceMarineRepository,
                                  CoordinatesRepository coordinatesRepository,
                                  ChapterRepository chapterRepository,
                                  UserRepository userRepository,
                                  MarineFilterStrategyFactory strategyFactory,
                                  JwtService jwtService) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.strategyFactory = strategyFactory;
        this.jwtService = jwtService;
    }

    private SpaceMarineResponseDTO toDto(SpaceMarine marine) {
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
        return toDto(marine);
    }

    @Override
    @Transactional
    public SpaceMarineResponseDTO createSpaceMarine(SpaceMarineCreateDTO dto, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Chapter chapter = chapterRepository.findById(dto.chapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + dto.chapterId()));

        float x = dto.coordinates().x();
        float y = dto.coordinates().y();

        //TODO: согласовать ограничения уникальности.
        for (SpaceMarine existing : spaceMarineRepository.findAllWithCoordinates()) {
            Coordinates exCoords = existing.getCoordinates();
            if (exCoords == null) continue;

            double dx = exCoords.getX() - dto.coordinates().x();
            double dy = exCoords.getY() - dto.coordinates().y();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (Math.abs(dx) < 1e-6 && Math.abs(dy) < 1e-6) {
                throw new IllegalArgumentException("Координаты уже заняты другим бойцом");
            }

            double minDistance = existing.getCategory().getRadius() + dto.category().getRadius();
            if (distance < minDistance) {
                throw new IllegalArgumentException(
                        String.format("Нарушение пространственного ограничения: %s слишком близко к %s (%.2f < %.2f)",
                                dto.name(), existing.getName(), distance, minDistance)
                );
            }
        }


        if (spaceMarineRepository.existsByNameAndChapterId(dto.name(), chapter.getId())) {
            throw new IllegalArgumentException("В главе '" + chapter.getName() +
                    "' уже существует боец с именем '" + dto.name() + "'");
        }

        long marineCount = spaceMarineRepository.countByChapterId(chapter.getId());
        if (marineCount >= 100) {
            throw new IllegalArgumentException("Глава '" + chapter.getName() +
                    "' достигла максимального лимита бойцов (100)");
        }

        Coordinates coordinates = new Coordinates(x, y);
        coordinatesRepository.save(coordinates);

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
        return toDto(marine);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SpaceMarineResponseDTO updateSpaceMarine(Long id, SpaceMarineUpdateDTO dto, Long userId) {
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));

        if (!marine.getOwner().getId().equals(userId)) {
            throw new SecurityException("You are not the owner of this SpaceMarine!");
        }

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
        return toDto(marine);
    }

    @Override
    public void deleteSpaceMarine(Long id, Long userId) {
        SpaceMarine marine = spaceMarineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceMarine not found with id " + id));

        if (!marine.getOwner().getId().equals(userId)) {
            throw new SecurityException("You are not the owner of this SpaceMarine!");
        }

        spaceMarineRepository.delete(marine);
    }

    @Override
    public List<SpaceMarineResponseDTO> findByCategory(AstartesCategory category) {
        return spaceMarineRepository.findByCategory(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceMarineResponseDTO> findByHealthGreaterThanEqual(Double health) {
        return spaceMarineRepository.findByHealthGreaterThanEqual(health).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceMarineResponseDTO> findByIsLoyal(Boolean loyal) {
        return spaceMarineRepository.findByIsLoyal(loyal).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceMarineResponseDTO> findByChapterName(String chapterName) {
        return spaceMarineRepository.findByChapterNameContainingIgnoreCase(chapterName).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SpaceMarineResponseDTO> getAllSpaceMarines(Pageable pageable) {
        return spaceMarineRepository.findAll(pageable)
                .map(this::toDto);
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

        return toDto(marine);

    }

    @Override
    public Page<SpaceMarineResponseDTO> getAllFilteredAndSorted(SpaceMarineQueryParams params) {
        int size = Math.min(params.size(), MAX_PAGE_SIZE);

        Sort sort = params.sortDir().equalsIgnoreCase("asc")
                ? Sort.by(params.sortBy()).ascending()
                : Sort.by(params.sortBy()).descending();

        Pageable pageable = PageRequest.of(params.page(), size, sort);

        Specification<SpaceMarine> spec = null;
        if (params.filterField() != null && params.filterValue() != null) {
            spec = strategyFactory.getSpecification(params.filterField(), params.filterValue());
        }

        Page<SpaceMarine> marines = (spec != null)
                ? spaceMarineRepository.findAll(spec, pageable)
                : spaceMarineRepository.findAll(pageable);

        return marines.map(this::toDto);
    }
}
