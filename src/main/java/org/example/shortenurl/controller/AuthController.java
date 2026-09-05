package org.example.shortenurl.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shortenurl.dtos.AuthRequest;
import org.example.shortenurl.dtos.LoginResponse;
import org.example.shortenurl.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        logRequestStarted("register");
        authService.register(authRequest);
        logRequestCompleted("register", HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        logRequestStarted("login");
        LoginResponse response = authService.login(authRequest);
        logRequestCompleted("login", HttpStatus.OK);
        return ResponseEntity.ok(response);
    }

    private void logRequestStarted(String operation) {
        log.info("Auth request started operation={}", operation);
    }

    private void logRequestCompleted(String operation, HttpStatus status) {
        log.info("Auth request completed operation={} status={}", operation, status.value());
    }
}
