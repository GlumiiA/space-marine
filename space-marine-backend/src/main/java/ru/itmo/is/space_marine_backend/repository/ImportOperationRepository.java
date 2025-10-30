package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;

import java.util.List;

public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {
    List<ImportOperation> findByUsername(String username);
}

