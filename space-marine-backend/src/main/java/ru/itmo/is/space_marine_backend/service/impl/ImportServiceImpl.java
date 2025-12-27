package ru.itmo.is.space_marine_backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.space_marine_backend.dto.request.ChapterCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineImportDTO;
import ru.itmo.is.space_marine_backend.entity.*;
import ru.itmo.is.space_marine_backend.repository.*;
import ru.itmo.is.space_marine_backend.service.ImportService;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ImportServiceImpl implements ImportService {
    private final SpaceMarineRepository spaceMarineRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final ImportOperationRepository importOperationRepository;
    private final ImportTransactionCoordinator coordinator;
    private static final double COORDINATE_EPSILON = 1e-6;

    public ImportServiceImpl(SpaceMarineRepository spaceMarineRepository,
            CoordinatesRepository coordinatesRepository,
            ChapterRepository chapterRepository,
            UserRepository userRepository,
            ImportOperationRepository importOperationRepository,
            ImportTransactionCoordinator coordinator) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.importOperationRepository = importOperationRepository;
        this.coordinator = coordinator;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void importFromDTOs(List<SpaceMarineImportDTO> dtos, Long userId, Long importOperationId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ImportOperation operation;
        if (importOperationId != null) {
            operation = importOperationRepository.findById(importOperationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "ImportOperation not found for id=" + importOperationId));
        } else {
            operation = new ImportOperation();
            operation.setUsername(owner.getUsername());
            operation.setTimestamp(LocalDateTime.now());
            operation.setStatus(ImportStatus.IN_PROGRESS);
            operation.setAddedCount(0);
        }

        List<SpaceMarine> marines = new ArrayList<>();
        try {
            for (SpaceMarineImportDTO dto : dtos) {
                SpaceMarine marine = convertDTOtoEntity(dto, owner);
                marines.add(marine);
            }

            validateMarines(marines);

            for (SpaceMarine marine : marines) {
                if (marine.getChapter().getId() == null) {
                    chapterRepository.save(marine.getChapter());
                }
                coordinatesRepository.save(marine.getCoordinates());
                spaceMarineRepository.save(marine);
            }
            operation.setStatus(ImportStatus.SUCCESS);
            operation.setAddedCount(marines.size());
            if (importOperationId != null) {
                coordinator.commit(importOperationId);
            }
        } catch (Exception e) {
            operation.setStatus(ImportStatus.FAILED);
            operation.setAddedCount(0);
            if (importOperationId != null) {
                try {
                    coordinator.rollback(importOperationId);
                } catch (Exception ex) {
                }
            }
            throw e;
        } finally {
            importOperationRepository.save(operation);
        }
    }

    private SpaceMarine convertDTOtoEntity(SpaceMarineImportDTO dto, User owner) {
        SpaceMarine marine = new SpaceMarine();
        marine.setOwner(owner);
        marine.setName(dto.name());
        marine.setHealth(dto.health());
        marine.setLoyal(dto.isLoyal());
        marine.setAchievements(dto.achievements());
        marine.setCategory(dto.category());

        Coordinates coords = new Coordinates(dto.coordinates().x(), dto.coordinates().y());
        marine.setCoordinates(coords);

        Chapter chapter;
        if (dto.chapterId() != 0) {
            chapter = chapterRepository.findById(dto.chapterId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Chapter not found with id " + dto.chapterId()));
        } else if (dto.chapter() != null) {
            ChapterCreateDTO c = dto.chapter();
            chapter = new Chapter();
            chapter.setName(c.name());
            chapter.setParentLegion(c.parentLegion());
            chapter.setWorld(String.valueOf(c.world()));
        } else {
            throw new IllegalArgumentException("Необходимо указать либо chapterId, либо chapter объект");
        }
        marine.setChapter(chapter);

        return marine;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void validateMarines(List<SpaceMarine> marines) {
        for (SpaceMarine marine : marines) {
            Coordinates coords = marine.getCoordinates();
            Chapter chapter = marine.getChapter();

            for (SpaceMarine existing : spaceMarineRepository.findAllWithCoordinates()) {
                Coordinates exCoords = existing.getCoordinates();
                if (exCoords == null)
                    continue;

                double dx = exCoords.getX() - coords.getX();
                double dy = exCoords.getY() - coords.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (Math.abs(dx) < COORDINATE_EPSILON && Math.abs(dy) < COORDINATE_EPSILON) {
                    throw new IllegalArgumentException(
                            "Координаты уже заняты другим бойцом: " + existing.getName());
                }

                double minDistance = existing.getCategory().getRadius() + marine.getCategory().getRadius();
                if (distance < minDistance) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Нарушение пространственного ограничения: %s слишком близко к %s (%.2f < %.2f)",
                                    marine.getName(), existing.getName(), distance, minDistance));
                }
            }

            if (chapter.getId() != null && spaceMarineRepository
                    .existsByNameAndChapterId(marine.getName(), chapter.getId())) {
                throw new IllegalArgumentException(
                        "В главе '" + chapter.getName() + "' уже существует боец с именем '" + marine.getName() + "'");
            }

            long marineCount = chapter.getId() != null ? spaceMarineRepository.countByChapterId(chapter.getId()) : 0;
            if (marineCount >= 100) {
                throw new IllegalArgumentException(
                        "Глава '" + chapter.getName() + "' достигла максимального лимита бойцов (100)");
            }
        }
    }
}