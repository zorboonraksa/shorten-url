package org.example.shortenurl.repository.mapper;

import org.example.shortenurl.model.ShortenedUrl;
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
class ShortenedUrlRowMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-05T00:00:00Z");

    @Mock
    private ResultSet resultSet;

    @Test
    void mapsEveryColumn() throws Exception {
        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getLong("user_id")).thenReturn(7L);
        when(resultSet.getString("original_url")).thenReturn("https://example.com");
        when(resultSet.getString("short_code")).thenReturn("abc1234");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.from(CREATED_AT));

        ShortenedUrl result = new ShortenedUrlRowMapper().mapRow(resultSet, 0);

        assertEquals(10L, result.id());
        assertEquals(7L, result.userId());
        assertEquals("https://example.com", result.originalUrl());
        assertEquals("abc1234", result.shortCode());
        assertEquals(CREATED_AT, result.createdAt());
    }
}
