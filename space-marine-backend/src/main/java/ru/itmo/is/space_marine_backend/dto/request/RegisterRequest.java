package ru.itmo.is.space_marine_backend.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
