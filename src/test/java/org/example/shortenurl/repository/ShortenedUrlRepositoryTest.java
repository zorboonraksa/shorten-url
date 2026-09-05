package org.example.shortenurl.repository;

import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.repository.mapper.ShortenedUrlRowMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortenedUrlRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ShortenedUrlRowMapper rowMapper;

    @InjectMocks
    private ShortenedUrlRepository repository;

    @Test
    void saveUsesUrlValuesAndReturnsSavedRow() {
        ShortenedUrl input = ShortenedUrl.builder()
                .userId(7L)
                .originalUrl("https://example.com")
                .shortCode("abc1234")
                .build();
        ShortenedUrl expected = ShortenedUrl.builder().id(10L).build();
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        when(jdbcTemplate.queryForObject(anyString(), parameters.capture(), eq(rowMapper)))
                .thenReturn(expected);

        ShortenedUrl result = repository.save(input);

        assertSame(expected, result);
        assertEquals(7L, parameters.getValue().getValue("userId"));
        assertEquals("https://example.com", parameters.getValue().getValue("originalUrl"));
        assertEquals("abc1234", parameters.getValue().getValue("shortCode"));
    }

    @Test
    void findByShortCodeReturnsFirstMatch() {
        ShortenedUrl expected = ShortenedUrl.builder().id(10L).build();
        when(jdbcTemplate.query(anyString(), eq(Map.of("shortCode", "abc1234")), eq(rowMapper)))
                .thenReturn(List.of(expected));

        Optional<ShortenedUrl> result = repository.findByShortCode("abc1234");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
    }

    @Test
    void findByShortCodeReturnsEmptyWhenMissing() {
        when(jdbcTemplate.query(anyString(), anyMap(), eq(rowMapper))).thenReturn(List.of());

        assertTrue(repository.findByShortCode("missing").isEmpty());
    }

    @Test
    void findAllByUserIdReturnsQueryResult() {
        List<ShortenedUrl> expected = List.of(ShortenedUrl.builder().id(10L).build());
        when(jdbcTemplate.query(anyString(), eq(Map.of("userId", 7L)), eq(rowMapper)))
                .thenReturn(expected);

        assertSame(expected, repository.findAllByUserId(7L));
    }

    @Test
    void deleteUsesUrlAndOwnerIds() {
        when(jdbcTemplate.update(anyString(), eq(Map.of("id", 10L, "userId", 7L))))
                .thenReturn(1);

        assertEquals(1, repository.deleteByIdAndUserId(10L, 7L));
        verify(jdbcTemplate).update(anyString(), eq(Map.of("id", 10L, "userId", 7L)));
    }
}
