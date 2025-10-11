package ru.itmo.is.space_marine_backend.dto.response;

public record ApiMessage(String code, String message) {
    public static ApiMessage success(String message) {
        return new ApiMessage("SUCCESS", message);
    }
}