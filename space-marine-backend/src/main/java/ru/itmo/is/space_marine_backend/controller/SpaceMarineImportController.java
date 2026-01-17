package ru.itmo.is.space_marine_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.space_marine_backend.dto.request.ChapterCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.CoordinatesCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineImportDTO;
import ru.itmo.is.space_marine_backend.dto.response.ApiError;
import ru.itmo.is.space_marine_backend.dto.response.ApiMessage;
import ru.itmo.is.space_marine_backend.dto.response.JwtUser;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;
import ru.itmo.is.space_marine_backend.service.ImportService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/space-marines/import")
public class SpaceMarineImportController {
    private final ImportService importService;

    public SpaceMarineImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<?> importMarines(@RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtUser user,
            @RequestParam(value = "importOperationId", required = false) Long importOperationId) {
        List<SpaceMarineImportDTO> marines;
        try {
            marines = parseFileToDTO(file);
            importService.importFromDTOs(marines, user.id(), importOperationId);
            return ResponseEntity.ok(ApiMessage.success("Импорт успешно завершён"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError("INVALID_DATA", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("INTERNAL_ERROR", "Неизвестная ошибка сервера"));
        }
    }

    public List<SpaceMarineImportDTO> parseFileToDTO(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<SpaceMarineImportDTO> dtos = new ArrayList<>();

        JsonNode root;
        try (InputStream is = file.getInputStream()) {
            root = mapper.readTree(is);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Файл имеет неверную структуру JSON", e);
        }

        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON должен быть массивом объектов" + root.getNodeType());
        }

        for (JsonNode node : root) {
            if (!node.hasNonNull("name") || node.get("name").asText().isBlank()) {
                throw new IllegalArgumentException("Поле 'name' обязательно и не должно быть пустым");
            }
            if (!node.hasNonNull("coordinates") || !node.get("coordinates").hasNonNull("x")
                    || !node.get("coordinates").hasNonNull("y")) {
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

            CoordinatesCreateDTO coords = new CoordinatesCreateDTO(
                    (float) node.get("coordinates").get("x").asDouble(),
                    (float) node.get("coordinates").get("y").asDouble());

            ChapterCreateDTO chapterDto = null;
            long chapterId = 0;
            if (node.has("chapterId")) {
                chapterId = node.get("chapterId").asLong();
            } else if (node.has("chapter")) {
                JsonNode ch = node.get("chapter");
                chapterDto = new ChapterCreateDTO(
                        ch.get("name").asText(),
                        ch.hasNonNull("parentLegion") ? ch.get("parentLegion").asText() : null,
                        ch.hasNonNull("world") ? java.util.Optional.ofNullable(ch.get("world").asText()) : null);
            }

            SpaceMarineImportDTO dto = new SpaceMarineImportDTO(
                    node.get("name").asText(),
                    coords,
                    chapterId,
                    chapterDto,
                    node.hasNonNull("health") ? node.get("health").asDouble() : 100.0,
                    node.hasNonNull("loyal") && node.get("loyal").asBoolean(),
                    node.get("achievements").asText(),
                    AstartesCategory.valueOf(node.get("category").asText()));

            dtos.add(dto);
        }

        return dtos;
    }
}
