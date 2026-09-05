package org.example.shortenurl.repository.mapper;

import org.example.shortenurl.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRowMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-05T00:00:00Z");

    @Mock
    private ResultSet resultSet;

    @Test
    void mapsEveryColumn() throws Exception {
        when(resultSet.getLong("id")).thenReturn(7L);
        when(resultSet.getString("email")).thenReturn("user@example.com");
        when(resultSet.getString("password_hash")).thenReturn("password-hash");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.from(CREATED_AT));

        User result = new UserRowMapper().mapRow(resultSet, 0);

        assert result != null;
        assertEquals(Long.valueOf(7L), result.id());
        assertEquals("user@example.com", result.email());
        assertEquals("password-hash", result.passwordHash());
        assertEquals(CREATED_AT, result.createdAt());
    }
}
