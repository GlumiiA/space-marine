package ru.itmo.is.space_marine_backend.dto.response;

import lombok.Data;


public class AuthResponseDTO {
    private String token;
    public AuthResponseDTO(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
}
