package ru.itmo.is.space_marine_backend.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.SpaceMarineResponseDTO;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;
import ru.itmo.is.space_marine_backend.service.SpaceMarineService;

import java.util.List;

@RestController
@RequestMapping("/api/space-marines")
public class SpaceMarineController {

    private final SpaceMarineService spaceMarineService;

    public SpaceMarineController(SpaceMarineService spaceMarineService) {
        this.spaceMarineService = spaceMarineService;
    }

    //  Получение объекта по ID
    @GetMapping("/{id}")
    public ResponseEntity<SpaceMarineResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceMarineService.getSpaceMarineById(id));
    }

    //  Создание нового объекта
    @PostMapping
    public ResponseEntity<SpaceMarineResponseDTO> create(@Valid @RequestBody SpaceMarineCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceMarineService.createSpaceMarine(dto));
    }

    //  Обновление объекта по ID
    @PutMapping("/{id}")
    public ResponseEntity<SpaceMarineResponseDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody SpaceMarineUpdateDTO dto) {
        return ResponseEntity.ok(spaceMarineService.updateSpaceMarine(id, dto));
    }

    //  Удаление объекта по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        spaceMarineService.deleteSpaceMarine(id);
        return ResponseEntity.noContent().build();
    }

    //  6.1. Сумма health всех объектов
    @GetMapping("/stats/total-health")
    public ResponseEntity<Double> getTotalHealth() {
        return ResponseEntity.ok(spaceMarineService.calculateTotalHealth());
    }

    //  6.2. Среднее значение health
    @GetMapping("/stats/average-health")
    public ResponseEntity<Double> getAverageHealth() {
        return ResponseEntity.ok(spaceMarineService.calculateAverageHealth());
    }

    //  6.3. Объект с минимальными coordinates
    @GetMapping("/min-coordinates")
    public ResponseEntity<SpaceMarineResponseDTO> getMarineWithMinCoordinates() {
        return ResponseEntity.ok(spaceMarineService.findMarineWithMinCoordinates());
    }

    //  6.4. Фильтрация по категории
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SpaceMarineResponseDTO>> getByCategory(@PathVariable AstartesCategory category) {
        return ResponseEntity.ok(spaceMarineService.findByCategory(category));
    }

    //  6.5. Фильтрация по здоровью (больше или равно)
    @GetMapping("/health/greater-than/{health}")
    public ResponseEntity<List<SpaceMarineResponseDTO>> getByHealth(@PathVariable Double health) {
        return ResponseEntity.ok(spaceMarineService.findByHealthGreaterThanEqual(health));
    }

    //  6.6. Фильтрация по лояльности
    @GetMapping("/loyal/{loyal}")
    public ResponseEntity<List<SpaceMarineResponseDTO>> getByLoyal(@PathVariable Boolean loyal) {
        return ResponseEntity.ok(spaceMarineService.findByLoyal(loyal));
    }

    //  6.7. Поиск по имени главы
    @GetMapping("/chapter/{chapterName}")
    public ResponseEntity<List<SpaceMarineResponseDTO>> getByChapterName(@PathVariable String chapterName) {
        return ResponseEntity.ok(spaceMarineService.findByChapterName(chapterName));
    }
}