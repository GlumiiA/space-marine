package ru.itmo.is.space_marine_backend.dto.response;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;

    public UserResponseDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}