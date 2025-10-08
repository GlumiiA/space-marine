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
        SpaceMarineResponseDTO dto = new SpaceMarineResponseDTO();
        dto.setId(marine.getId());
        dto.setName(marine.getName());
        dto.setCreationDate(marine.getCreationDate());
        dto.setHealth(marine.getHealth());
        dto.setIsLoyal(marine.getIsLoyal());
        dto.setAchievements(marine.getAchievements());
        dto.setCategory(marine.getCategory());

        if (marine.getCoordinates() != null) {
            dto.setCoordinates(new CoordinatesResponseDTO(
                    marine.getCoordinates().getX(),
                    marine.getCoordinates().getY()
            ));
        }

        if (marine.getChapter() != null) {
            dto.setChapter(new ChapterResponseDTO(
                    marine.getChapter().getName(),
                    marine.getChapter().getParentLegion(),
                    marine.getChapter().getWorld(),
                    marine.getChapter().getMarinesCount()
            ));
        }

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
        marine.setIsLoyal(dto.getIsLoyal());
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
        marine.setIsLoyal(dto.getIsLoyal());
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
