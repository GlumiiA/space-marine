package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.is.space_marine_backend.entity.ImportFile;
import ru.itmo.is.space_marine_backend.entity.StorageStatus;

import java.util.List;

public interface ImportFileRepository extends JpaRepository<ImportFile, Long> {
    List<ImportFile> findByImportOperationIdAndStorageStatus(Long importId, StorageStatus status);
    List<ImportFile> findByImportOperationId(Long importId);
}
