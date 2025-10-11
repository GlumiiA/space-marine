package ru.itmo.is.space_marine_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.auth.JwtProvider;
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

    public void register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username is already in use");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public AuthResponseDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect password");
        }
        return new AuthResponseDTO(jwtProvider.generateToken(user.getId(), user.getUsername()));
    }
}
