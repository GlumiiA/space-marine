package ru.itmo.is.space_marine_backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ImportTransactionCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ImportTransactionCoordinator.class);
    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final MinioService minioService;
    private final ImportOperationRepository importRepo;
    private final ImportFileRepository importFileRepo;
    private final Clock clock;
    private final Supplier<UUID> uuidGenerator;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public ImportTransactionCoordinator(MinioService minioService,
            ImportOperationRepository importRepo,
            ImportFileRepository importFileRepo,
            Clock clock,
            Supplier<UUID> uuidGenerator) {
        this.minioService = minioService;
        this.importRepo = importRepo;
        this.importFileRepo = importFileRepo;
        this.clock = clock;
        this.uuidGenerator = uuidGenerator;
    }

    public ImportOperation prepare(String username, InputStream fileStream, String filename, String contentType,
            long size) {
        ImportOperation op = new ImportOperation();
        op.setUsername(username);
        op.setTimestamp(LocalDateTime.now(clock));
        op.setStatus(ImportStatus.IN_PROGRESS);
        op.setAddedCount(0);
        op = importRepo.save(op);

        String txId = uuidGenerator.get().toString();
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
        if (files.size() > 1)
            throw new IllegalStateException("Multiple prepared files found, expected exactly one");
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
            if (files.size() > 1) {
                log.warn("Found {} prepared files for import operation {}, expected exactly one. Rolling back first.",
                        files.size(), importOperationId);
            }

            ImportFile f = files.get(0);
            try {
                minioService.delete(f.getObjectKey());
                f.setStorageStatus(StorageStatus.ROLLED_BACK);
                importFileRepo.save(f);
            } catch (Exception e) {
                log.error("Failed to delete object key {} for import file {}: {}",
                        f.getObjectKey(), f.getId(), e.getMessage(), e);
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
        if (committed.size() > 1)
            throw new IllegalStateException("Multiple committed files found, expected exactly one");
        ImportFile f = committed.get(0);
        return minioService.presignGetUrl(f.getObjectKey(), PRESIGNED_URL_EXPIRATION, f.getFileName());
    }

    public Optional<ImportOperation> find(Long id) {
        return importRepo.findById(id);
    }
}
