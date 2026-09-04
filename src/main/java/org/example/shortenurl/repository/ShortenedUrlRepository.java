package org.example.shortenurl.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.repository.mapper.ShortenedUrlRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShortenedUrlRepository {

    private static final String SHORTENED_URL_COLUMNS = """
            id, user_id, original_url, short_code, created_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ShortenedUrlRowMapper rowMapper;

    public ShortenedUrl save(ShortenedUrl shortenedUrl) {
        String sql = """
                INSERT INTO shortened_urls (user_id, original_url, short_code)
                VALUES (:userId, :originalUrl, :shortCode)
                RETURNING %s
                """.formatted(SHORTENED_URL_COLUMNS);

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("userId", shortenedUrl.userId())
                .addValue("originalUrl", shortenedUrl.originalUrl())
                .addValue("shortCode", shortenedUrl.shortCode());

        return jdbcTemplate.queryForObject(sql, parameters, rowMapper);
    }

    public Optional<ShortenedUrl> findByShortCode(String shortCode) {
        String sql = """
                SELECT %s
                FROM shortened_urls
                WHERE short_code = :shortCode
                """.formatted(SHORTENED_URL_COLUMNS);

        return firstOrEmpty(jdbcTemplate.query(
                sql,
                Map.of("shortCode", shortCode),
                rowMapper
        ));
    }

    public List<ShortenedUrl> findAllByUserId(Long userId) {
        String sql = """
                SELECT %s
                FROM shortened_urls
                WHERE user_id = :userId
                ORDER BY created_at DESC
                """.formatted(SHORTENED_URL_COLUMNS);

        return jdbcTemplate.query(sql, Map.of("userId", userId), rowMapper);
    }

    public int deleteByIdAndUserId(Long id, Long userId) {
        String sql = """
                DELETE FROM shortened_urls
                WHERE id = :id
                  AND user_id = :userId
                """;

        return jdbcTemplate.update(sql, Map.of(
                "id", id,
                "userId", userId
        ));
    }

    private Optional<ShortenedUrl> firstOrEmpty(List<ShortenedUrl> shortenedUrls) {
        return shortenedUrls.stream().findFirst();
    }
}
