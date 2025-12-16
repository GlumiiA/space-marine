package ru.itmo.is.space_marine_backend.service;

import org.springframework.security.core.Authentication;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;

import java.util.List;

public interface ImportHistoryService {
    List<ImportOperation> getHistory(Authentication auth);
}
