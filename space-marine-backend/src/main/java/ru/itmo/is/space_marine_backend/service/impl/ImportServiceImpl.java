package ru.itmo.is.space_marine_backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.space_marine_backend.entity.*;
import ru.itmo.is.space_marine_backend.repository.*;
import ru.itmo.is.space_marine_backend.service.ImportService;

import java.io.IOException;
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

    public ImportServiceImpl(SpaceMarineRepository spaceMarineRepository,
                                    CoordinatesRepository coordinatesRepository,
                                    ChapterRepository chapterRepository,
                                    UserRepository userRepository,
                             ImportOperationRepository importOperationRepository) {
        this.spaceMarineRepository = spaceMarineRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.importOperationRepository = importOperationRepository;
    }

    @Override
    public void importFromJson(MultipartFile file, Long userId) throws IOException {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        ImportOperation operation = new ImportOperation();
        operation.setUsername(currentUser.getUsername());
        operation.setTimestamp(LocalDateTime.now());

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file.getInputStream());

            if (!root.isArray()) {
                throw new IllegalArgumentException("JSON должен быть массивом объектов");
            }

            List<SpaceMarine> marines = parseAndValidate(root, currentUser);

            for (SpaceMarine marine : marines) {
                if (marine.getChapter().getId() == null) {
                    chapterRepository.save(marine.getChapter());
                }
                coordinatesRepository.save(marine.getCoordinates());
                spaceMarineRepository.save(marine);
            }

            operation.setStatus("SUCCESS");
            operation.setAddedCount(marines.size());

        } catch (Exception e) {
            operation.setStatus("FAILED");
            operation.setAddedCount(0);
            throw e;
        } finally {
            importOperationRepository.save(operation);
        }
    }

    @Override
    public List<SpaceMarine> parseAndValidate(JsonNode root, User owner) {
        List<SpaceMarine> marines = new ArrayList<>();

        for (JsonNode node : root) {
            if (!node.hasNonNull("name") || node.get("name").asText().isBlank()) {
                throw new IllegalArgumentException("Поле 'name' обязательно и не должно быть пустым");
            }
            if (!node.hasNonNull("coordinates") || !node.get("coordinates").hasNonNull("x") || !node.get("coordinates").hasNonNull("y")) {
                throw new IllegalArgumentException("Поле 'coordinates' обязательно и должно содержать 'x' и 'y'");
            }
            if (!node.has("chapterId") && !node.has("chapter")) {
                throw new IllegalArgumentException("Необходимо указать либо 'chapterId', либо объект 'chapter'");
            }
            if (!node.hasNonNull("achievements") || node.get("achievements").asText().isBlank()) {
                throw new IllegalArgumentException("Поле 'achievements' обязательно");
            }
            if (!node.hasNonNull("category")) {
                throw new IllegalArgumentException("Поле 'category' обязательно");
            }

            SpaceMarine marine = new SpaceMarine();
            marine.setName(node.get("name").asText());
            marine.setHealth(node.hasNonNull("health") ? node.get("health").asDouble() : 100.0);
            marine.setLoyal(node.hasNonNull("loyal") && node.get("loyal").asBoolean());
            marine.setAchievements(node.get("achievements").asText());
            marine.setOwner(owner);

            JsonNode coordsNode = node.get("coordinates");
            Coordinates coords = new Coordinates(
                    (float) coordsNode.get("x").asDouble(),
                    (float) coordsNode.get("y").asDouble()
            );
            marine.setCoordinates(coords);

            Chapter chapter;
            if (node.has("chapterId")) {
                Long chapterId = node.get("chapterId").asLong();
                chapter = chapterRepository.findById(chapterId)
                        .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id " + chapterId));
            } else {
                JsonNode chapterNode = node.get("chapter");
                chapter = new Chapter();
                chapter.setName(chapterNode.get("name").asText());
                chapter.setParentLegion(chapterNode.hasNonNull("parentLegion") ? chapterNode.get("parentLegion").asText() : null);
                chapter.setWorld(chapterNode.hasNonNull("world") ? chapterNode.get("world").asText() : null);
                chapter.setMarinesCount(chapterNode.hasNonNull("marinesCount") ? chapterNode.get("marinesCount").asInt() : 0);
            }
            marine.setChapter(chapter);

            try {
                marine.setCategory(AstartesCategory.valueOf(node.get("category").asText()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неверное значение для 'category'");
            }

            // проверки уникальности
            for (SpaceMarine existing : spaceMarineRepository.findAllWithCoordinates()) {
                Coordinates exCoords = existing.getCoordinates();
                if (exCoords == null) continue;

                double dx = exCoords.getX() - coords.getX();
                double dy = exCoords.getY() - coords.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (Math.abs(dx) < 1e-6 && Math.abs(dy) < 1e-6) {
                    throw new IllegalArgumentException(
                            "Координаты уже заняты другим бойцом: " + existing.getName()
                    );
                }

                double minDistance = existing.getCategory().getRadius() + marine.getCategory().getRadius();
                if (distance < minDistance) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Нарушение пространственного ограничения: %s слишком близко к %s (%.2f < %.2f)",
                                    marine.getName(), existing.getName(), distance, minDistance
                            )
                    );
                }
            }

            if (chapter.getId() != null && spaceMarineRepository.existsByNameAndChapterId(marine.getName(), chapter.getId())) {
                throw new IllegalArgumentException(
                        "В главе '" + chapter.getName() + "' уже существует боец с именем '" + marine.getName() + "'"
                );
            }

            long marineCount = chapter.getId() != null ? spaceMarineRepository.countByChapterId(chapter.getId()) : 0;
            if (marineCount >= 100) {
                throw new IllegalArgumentException(
                        "Глава '" + chapter.getName() + "' достигла максимального лимита бойцов (100)"
                );
            }

            marines.add(marine);
        }

        return marines;
    }

}
