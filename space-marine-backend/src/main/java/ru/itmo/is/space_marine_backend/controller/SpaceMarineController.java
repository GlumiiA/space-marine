package ru.itmo.is.space_marine_backend.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineCreateDTO;
import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineUpdateDTO;
import ru.itmo.is.space_marine_backend.dto.response.JwtUser;
import ru.itmo.is.space_marine_backend.dto.response.PageDTO;
import ru.itmo.is.space_marine_backend.dto.response.SpaceMarineQueryParams;
import ru.itmo.is.space_marine_backend.dto.response.SpaceMarineResponseDTO;
import ru.itmo.is.space_marine_backend.service.SpaceMarineService;

import java.util.List;

@RestController
@RequestMapping("/api/space-marines")
public class SpaceMarineController {

    private final SpaceMarineService spaceMarineService;

    public SpaceMarineController(SpaceMarineService spaceMarineService) {
        this.spaceMarineService = spaceMarineService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceMarineResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceMarineService.getSpaceMarineById(id));
    }

    @PostMapping
    public ResponseEntity<SpaceMarineResponseDTO> createMarine(
            @Valid @RequestBody SpaceMarineCreateDTO dto,
            @AuthenticationPrincipal JwtUser user) {
        return ResponseEntity.ok(spaceMarineService.createSpaceMarine(dto, user.username()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceMarineResponseDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody SpaceMarineUpdateDTO dto,
                                                         @AuthenticationPrincipal JwtUser user) {
        return ResponseEntity.ok(spaceMarineService.updateSpaceMarine(id, dto, user.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal JwtUser user) {
        spaceMarineService.deleteSpaceMarine(id, user.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chapter/{chapterName}")
    public ResponseEntity<List<SpaceMarineResponseDTO>> getByChapterName(@PathVariable String chapterName) {
        return ResponseEntity.ok(spaceMarineService.findByChapterName(chapterName));
    }

    @PostMapping("/{id}/assign-chapter")
    public ResponseEntity<SpaceMarineResponseDTO> assignToChapter(
            @PathVariable Long id,
            @RequestParam Long chapterId,
            @AuthenticationPrincipal String username
    ) {
        try {
            SpaceMarineResponseDTO updated = spaceMarineService.assignMarineToChapter(id, chapterId, username);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<PageDTO<SpaceMarineResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String filterField,
            @RequestParam(required = false) String filterValue
    ) {
        SpaceMarineQueryParams pageRequest = new SpaceMarineQueryParams(
                page, size, sortBy, sortDir, filterField, filterValue
        );
        Page<SpaceMarineResponseDTO> marinesPage =
                spaceMarineService.getAllFilteredAndSorted(pageRequest);

        PageDTO<SpaceMarineResponseDTO> dto = new PageDTO<>(
                marinesPage.getContent(),
                marinesPage.getNumber(),
                marinesPage.getSize(),
                marinesPage.getTotalElements(),
                marinesPage.getTotalPages()
        );

        return ResponseEntity.ok(dto);
    }
}