package ru.itmo.is.space_marine_backend.service;

import ru.itmo.is.space_marine_backend.dto.request.SpaceMarineImportDTO;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

import java.io.IOException;
import java.util.List;

public interface ImportService {
    void importFromDTOs(List<SpaceMarineImportDTO> dtos, Long userId, Long importOperationId) throws IOException;

    void validateMarines(List<SpaceMarine> marines);

}
