package org.example.shortenurl.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shortenurl.dtos.AuthRequest;
import org.example.shortenurl.dtos.LoginResponse;
import org.example.shortenurl.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        authService.register(authRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        return ResponseEntity.ok(authService.login(authRequest));
    }
}
