package ru.itmo.is.space_marine_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.dto.response.JwtUser;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;
import ru.itmo.is.space_marine_backend.repository.ImportOperationRepository;
import ru.itmo.is.space_marine_backend.service.ImportHistoryService;

@Service
@RequiredArgsConstructor
public class ImportHistoryServiceImpl implements ImportHistoryService {

    private final ImportOperationRepository repo;

    @Override
    public Page<ImportOperation> getHistory(Authentication auth, Pageable pageable) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        return repo.findByUsername(user.username(), pageable);
    }
}