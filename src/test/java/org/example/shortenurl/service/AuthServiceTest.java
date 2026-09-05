package org.example.shortenurl.service;

import org.example.shortenurl.dtos.AuthRequest;
import org.example.shortenurl.dtos.LoginResponse;
import org.example.shortenurl.exception.ApiException;
import org.example.shortenurl.model.User;
import org.example.shortenurl.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void registerNormalizesEmailHashesPasswordAndSavesUser() {
        AuthRequest request = new AuthRequest(" User@Example.com ", "password123");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("password-hash");
        authService.register(request);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("user@example.com", userCaptor.getValue().email());
        assertEquals("password-hash", userCaptor.getValue().passwordHash());
    }

    @Test
    void registerReturnsConflictWhenEmailAlreadyExists() {
        AuthRequest request = new AuthRequest(" User@Example.com ", "password123");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already exists", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerReturnsConflictWhenConcurrentRequestCreatesSameEmail() {
        AuthRequest request = new AuthRequest("user@example.com", "password123");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("password-hash");
        doThrow(new DuplicateKeyException("duplicate email"))
                .when(userRepository).save(any(User.class));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        AuthRequest request = new AuthRequest(" User@Example.com ", "password123");
        User user = User.builder()
                .id(7L)
                .email("user@example.com")
                .passwordHash("password-hash")
                .build();
        LoginResponse expected = new LoginResponse("token", "Bearer", 3600);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "password-hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(expected);

        LoginResponse actual = authService.login(request);

        assertSame(expected, actual);
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void loginReturnsUnauthorizedWhenEmailDoesNotExist() {
        AuthRequest request = new AuthRequest("missing@example.com", "password123");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordIsWrong() {
        AuthRequest request = new AuthRequest("user@example.com", "wrong-password");
        User user = User.builder()
                .id(7L)
                .email("user@example.com")
                .passwordHash("password-hash")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "password-hash")).thenReturn(false);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }
}
