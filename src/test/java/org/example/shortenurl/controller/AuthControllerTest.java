package org.example.shortenurl.controller;

import org.example.shortenurl.dtos.AuthRequest;
import org.example.shortenurl.dtos.LoginResponse;
import org.example.shortenurl.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Test
    void registerReturnsCreated() {
        AuthController controller = new AuthController(authService);
        AuthRequest request = new AuthRequest("user@example.com", "password123");

        ResponseEntity<Void> response = controller.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService).register(request);
    }

    @Test
    void loginReturnsServiceResponse() {
        AuthController controller = new AuthController(authService);
        AuthRequest request = new AuthRequest("user@example.com", "password123");
        LoginResponse expected = new LoginResponse("token", "Bearer", 3600);
        when(authService.login(request)).thenReturn(expected);

        ResponseEntity<LoginResponse> response = controller.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
    }
}
