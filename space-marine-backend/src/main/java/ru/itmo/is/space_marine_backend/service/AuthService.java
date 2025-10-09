package ru.itmo.is.space_marine_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.auth.JwtProvider;
import ru.itmo.is.space_marine_backend.dto.request.LoginRequest;
import ru.itmo.is.space_marine_backend.dto.request.RegisterRequest;
import ru.itmo.is.space_marine_backend.dto.response.AuthResponseDTO;
import ru.itmo.is.space_marine_backend.entity.User;
import ru.itmo.is.space_marine_backend.repository.UserRepository;

@Service
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider provider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = provider;
    }

    public void register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
            throw new RuntimeException("Username is already in use");
        }
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        userRepository.save(user);
    }

    public AuthResponseDTO login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Incorrect password");
        }
        return new AuthResponseDTO(jwtProvider.generateToken(user.getUsername()));
    }
}
