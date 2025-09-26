package ru.itmo.is.space_marine_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordinatesCreateDTO {
    @NotNull(message = "X is required")
    private Float x; //Поле не может быть null

    @NotNull(message = "Y is required")
    private Float y; //Поле не может быть null
}
