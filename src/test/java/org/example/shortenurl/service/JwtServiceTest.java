package org.example.shortenurl.service;

import org.example.shortenurl.dtos.LoginResponse;
import org.example.shortenurl.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generatedTokenContainsUserAndExpirationClaims() {
        SecretKey secretKey = new SecretKeySpec(
                "test-secret-key-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        JwtService jwtService = new JwtService(
                NimbusJwtEncoder.withSecretKey(secretKey)
                        .algorithm(MacAlgorithm.HS256)
                        .build()
        );
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
        User user = User.builder()
                .id(7L)
                .email("user@example.com")
                .build();

        LoginResponse response = jwtService.generateToken(user);
        Jwt jwt = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(response.accessToken());

        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals("7", jwt.getSubject());
        assertEquals("user@example.com", jwt.getClaimAsString("email"));
        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();
        assertNotNull(issuedAt);
        assertNotNull(expiresAt);
        assertTrue(expiresAt.isAfter(issuedAt));
        assertEquals(3600L, Duration.between(issuedAt, expiresAt).getSeconds());
    }
}
