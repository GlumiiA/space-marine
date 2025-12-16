package ru.itmo.is.space_marine_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.dto.response.JwtUser;
import ru.itmo.is.space_marine_backend.entity.ImportOperation;
import ru.itmo.is.space_marine_backend.repository.ImportOperationRepository;
import ru.itmo.is.space_marine_backend.service.ImportHistoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryServiceImpl implements ImportHistoryService {

    private final ImportOperationRepository repo;

    @Override
    public List<ImportOperation> getHistory(Authentication auth) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        return repo.findByUsername(user.username());
    }
}