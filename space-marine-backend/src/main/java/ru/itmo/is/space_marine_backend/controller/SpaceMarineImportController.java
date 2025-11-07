package ru.itmo.is.space_marine_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.space_marine_backend.dto.response.ApiError;
import ru.itmo.is.space_marine_backend.dto.response.ApiMessage;
import ru.itmo.is.space_marine_backend.dto.response.JwtUser;
import ru.itmo.is.space_marine_backend.service.ImportService;

@RestController
@RequestMapping("/api/space-marines/import")
public class SpaceMarineImportController {
    private final ImportService importService;
    public SpaceMarineImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<?> importMarines(@RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal JwtUser user) {
        try {
            importService.importFromJson(file, user.id());
            return ResponseEntity.ok(ApiMessage.success("Импорт успешно завершён"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError("IMPORT_ERROR", e.getMessage()));
        }
    }
}
