package org.example.shortenurl.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void authenticationEntryPointWritesJsonUnauthorizedResponse() throws Exception {
        AuthenticationEntryPoint entryPoint = config.authenticationEntryPoint(objectMapper);
        MockHttpServletRequest request = request("/api/urls");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Authentication required")
        );

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Authentication required"));
        assertTrue(response.getContentAsString().contains("/api/urls"));
    }

    @Test
    void accessDeniedHandlerWritesJsonForbiddenResponse() throws Exception {
        AccessDeniedHandler handler = config.accessDeniedHandler(objectMapper);
        MockHttpServletRequest request = request("/api/shorten");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access denied"));

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Access denied"));
    }

    @Test
    void passwordEncoderUsesBcrypt() {
        PasswordEncoder encoder = config.passwordEncoder();
        String hash = encoder.encode("password123");

        assertTrue(encoder.matches("password123", hash));
    }

    @Test
    void jwtSecretMustContainAtLeastThirtyTwoBytes() {
        String encodedSecret = Base64.getEncoder().encodeToString(
                "too-short".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> config.jwtSecretKey(encodedSecret)
        );

        assertEquals("JWT_SECRET must contain at least 32 bytes", exception.getMessage());
    }

    @Test
    void createsJwtKeyEncoderAndDecoder() {
        String encodedSecret = Base64.getEncoder().encodeToString(
                "test-secret-key-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8)
        );

        SecretKey secretKey = config.jwtSecretKey(encodedSecret);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        JwtDecoder decoder = config.jwtDecoder(secretKey);

        assertEquals("HmacSHA256", secretKey.getAlgorithm());
        assertNotNull(encoder);
        assertNotNull(decoder);
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(path);
        return request;
    }
}
