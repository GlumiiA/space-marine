package ru.itmo.is.space_marine_backend.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.space_marine_backend.entity.ImportFile;
import ru.itmo.is.space_marine_backend.repository.ImportFileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.space_marine_backend.dto.response.ApiError;
import ru.itmo.is.space_marine_backend.dto.response.ApiMessage;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;
import ru.itmo.is.space_marine_backend.service.impl.ImportTransactionCoordinator;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportFileController {

    private final ImportTransactionCoordinator coordinator;
    private final ImportFileRepository importFileRepository;

    @PostMapping("/prepare")
    public ResponseEntity<?> prepare(@RequestParam("file") MultipartFile file,
            @RequestParam("username") String username) throws Exception {
        ImportOperation op = coordinator.prepare(username, file.getInputStream(), file.getOriginalFilename(),
                file.getContentType(), file.getSize());
        return ResponseEntity.ok(ApiMessage.success("Prepared import operation id=" + op.getId()));
    }

    @PostMapping("/commit/{id}")
    public ResponseEntity<?> commit(@PathVariable Long id) {
        try {
            ImportOperation op = coordinator.commit(id);
            return ResponseEntity.ok(ApiMessage.success("Commit successful for import id=" + op.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("COMMIT_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/rollback/{id}")
    public ResponseEntity<?> rollback(@PathVariable Long id) {
        try {
            ImportOperation op = coordinator.rollback(id);
            return ResponseEntity.ok(ApiMessage.success("Rollback completed for import id=" + op.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("ROLLBACK_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> presign(@PathVariable Long id) {
        try {
            String url = coordinator.presignUrl(id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiMessage.success(url));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_AVAILABLE", e.getMessage()));
        }
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<?> getFiles(@PathVariable Long id) {
        List<ImportFile> files = importFileRepository.findByImportOperationId(id);
        List<Map<String, Object>> out = files.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("fileName", f.getFileName());
            m.put("objectKey", f.getObjectKey());
            m.put("storageStatus", f.getStorageStatus());
            m.put("createdAt", f.getCreatedAt());
            m.put("txId", f.getTxId());
            m.put("ownerUsername", f.getOwnerUsername());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }
}
