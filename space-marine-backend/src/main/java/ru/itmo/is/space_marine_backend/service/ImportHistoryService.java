package ru.itmo.is.space_marine_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;

public interface ImportHistoryService {
    Page<ImportOperation> getHistory(Authentication auth, Pageable pageable);
}
