package ru.itmo.is.space_marine_backend.dto.response;


import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class CoordinatesResponseDTO {
    private Float x;
    private Float y;

    public CoordinatesResponseDTO(Float x, Float y) {
        this.x = x;
        this.y = y;
    }
}
