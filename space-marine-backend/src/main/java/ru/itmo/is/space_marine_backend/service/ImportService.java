package ru.itmo.is.space_marine_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;
import ru.itmo.is.space_marine_backend.entity.User;

import java.io.IOException;
import java.util.List;

public interface ImportService {
    void importFromJson(MultipartFile file, Long userId) throws IOException;

    List<SpaceMarine> parseAndValidate(JsonNode root, User owner);

}
