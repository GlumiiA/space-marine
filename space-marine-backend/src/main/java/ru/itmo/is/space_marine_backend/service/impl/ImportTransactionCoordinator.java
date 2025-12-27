package ru.itmo.is.space_marine_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.space_marine_backend.entity.ImportFile;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;
import ru.itmo.is.space_marine_backend.entity.ImportStatus;
import ru.itmo.is.space_marine_backend.entity.StorageStatus;
import ru.itmo.is.space_marine_backend.repository.ImportFileRepository;
import ru.itmo.is.space_marine_backend.repository.ImportOperationRepository;
import ru.itmo.is.space_marine_backend.service.MinioService;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImportTransactionCoordinator {

    private final MinioService minioService;
    private final ImportOperationRepository importRepo;
    private final ImportFileRepository importFileRepo;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public ImportTransactionCoordinator(MinioService minioService,
            ImportOperationRepository importRepo,
            ImportFileRepository importFileRepo) {
        this.minioService = minioService;
        this.importRepo = importRepo;
        this.importFileRepo = importFileRepo;
    }

    public ImportOperation prepare(String username, InputStream fileStream, String filename, String contentType,
            long size) {
        ImportOperation op = new ImportOperation();
        op.setUsername(username);
        op.setTimestamp(LocalDateTime.now());
        op.setStatus(ImportStatus.IN_PROGRESS);
        op.setAddedCount(0);
        op = importRepo.save(op);

        String txId = UUID.randomUUID().toString();
        String tempKey = null;
        try {
            tempKey = minioService.uploadTemp(txId, filename, fileStream, contentType, size);
        } catch (Exception e) {
            op.setStatus(ImportStatus.FAILED);
            importRepo.save(op);
            throw e;
        }

        ImportFile file = new ImportFile();
        file.setImportOperation(op);
        file.setTxId(txId);
        file.setOwnerUsername(username);
        file.setFileName(filename);
        file.setBucket(bucketName);
        file.setObjectKey(tempKey);
        file.setSizeBytes(size);
        file.setStorageStatus(StorageStatus.PREPARED);
        importFileRepo.save(file);

        return op;
    }

    @Transactional
    public ImportOperation commit(Long importOperationId) {
        ImportOperation op = importRepo.findById(importOperationId)
                .orElseThrow(() -> new IllegalArgumentException("ImportOperation not found"));
        List<ImportFile> files = importFileRepo.findByImportOperationIdAndStorageStatus(op.getId(),
                StorageStatus.PREPARED);
        if (files.isEmpty())
            throw new IllegalStateException("No prepared file to commit");
        ImportFile f = files.get(0);

        String finalKey = String.format("files/%d/%d/%s", op.getId(), f.getId(), f.getFileName());
        minioService.copyToFinal(f.getObjectKey(), finalKey);
        f.setObjectKey(finalKey);
        f.setStorageStatus(StorageStatus.COMMITTED);
        importFileRepo.save(f);

        op.setStatus(ImportStatus.SUCCESS);
        return importRepo.save(op);
    }

    @Transactional
    public ImportOperation rollback(Long importOperationId) {
        ImportOperation op = importRepo.findById(importOperationId)
                .orElseThrow(() -> new IllegalArgumentException("ImportOperation not found"));
        List<ImportFile> files = importFileRepo.findByImportOperationIdAndStorageStatus(op.getId(),
                StorageStatus.PREPARED);
        if (!files.isEmpty()) {
            ImportFile f = files.get(0);
            try {
                if (f.getObjectKey() != null) {
                    minioService.delete(f.getObjectKey());
                }
                f.setStorageStatus(StorageStatus.ROLLED_BACK);
                importFileRepo.save(f);
            } catch (Exception e) {
                f.setStorageStatus(StorageStatus.FAILED);
                importFileRepo.save(f);
            }
        }

        op.setStatus(ImportStatus.FAILED);
        return importRepo.save(op);
    }

    public String presignUrl(Long importOperationId) {
        ImportOperation op = importRepo.findById(importOperationId)
                .orElseThrow(() -> new IllegalArgumentException("ImportOperation not found"));
        List<ImportFile> committed = importFileRepo.findByImportOperationIdAndStorageStatus(op.getId(),
                StorageStatus.COMMITTED);
        if (committed.isEmpty())
            throw new IllegalStateException("No committed file");
        ImportFile f = committed.get(0);
        return minioService.presignGetUrl(f.getObjectKey(), java.time.Duration.ofMinutes(10), f.getFileName());
    }

    public Optional<ImportOperation> find(Long id) {
        return importRepo.findById(id);
    }
}
