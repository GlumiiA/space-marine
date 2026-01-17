package ru.itmo.is.space_marine_backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;
import ru.itmo.is.space_marine_backend.service.ImportHistoryService;

@RestController
@RequestMapping("/api/import")
public class ImportHistoryController {

    private final ImportHistoryService historyService;

    public ImportHistoryController(ImportHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ImportOperation>> getHistory(Authentication auth, Pageable pageable) {
        return ResponseEntity.ok(historyService.getHistory(auth, pageable));
    }
}