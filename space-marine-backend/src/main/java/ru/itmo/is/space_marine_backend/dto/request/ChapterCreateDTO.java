package ru.itmo.is.space_marine_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChapterCreateDTO {
    @NotBlank
    @NotNull(message = "Name is required")
    private String name; //Поле не может быть null, Строка не может быть пустой

    @NotNull(message = "ParentLegion is required")
    private String parentLegion;

    private String world; //Поле может быть null
}
