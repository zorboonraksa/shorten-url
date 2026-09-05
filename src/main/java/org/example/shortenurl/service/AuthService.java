package org.example.shortenurl.service;

import lombok.RequiredArgsConstructor;
import org.example.shortenurl.dtos.AuthRequest;
import org.example.shortenurl.dtos.LoginResponse;
import org.example.shortenurl.exception.ApiException;
import org.example.shortenurl.model.User;
import org.example.shortenurl.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(AuthRequest request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw emailAlreadyExists();
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        try {
            userRepository.save(user);
        } catch (DuplicateKeyException _) {
            throw emailAlreadyExists();
        }
    }

    public LoginResponse login(AuthRequest request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw invalidCredentials();
        }

        return jwtService.generateToken(user);
    }

    private ApiException emailAlreadyExists() {
        return new ApiException(
                HttpStatus.CONFLICT,
                "Email already exists"
        );
    }

    private ApiException invalidCredentials() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
    }
}
