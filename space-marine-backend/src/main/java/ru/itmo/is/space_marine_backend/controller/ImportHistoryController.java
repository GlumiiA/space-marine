package ru.itmo.is.space_marine_backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;
import ru.itmo.is.space_marine_backend.service.ImportHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/import")
public class ImportHistoryController {

    private final ImportHistoryService historyService;

    public ImportHistoryController(ImportHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportOperation>> getHistory(Authentication auth) {
        return ResponseEntity.ok(historyService.getHistory(auth));
    }
}