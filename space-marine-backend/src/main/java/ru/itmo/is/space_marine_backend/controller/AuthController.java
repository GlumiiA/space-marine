package ru.itmo.is.space_marine_backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.space_marine_backend.dto.request.LoginRequest;
import ru.itmo.is.space_marine_backend.dto.request.RegisterRequest;
import ru.itmo.is.space_marine_backend.dto.response.AuthResponseDTO;
import ru.itmo.is.space_marine_backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request.username(), request.password());
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }
}
