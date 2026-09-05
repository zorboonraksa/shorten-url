package org.example.shortenurl.repository;

import lombok.RequiredArgsConstructor;
import org.example.shortenurl.model.User;
import org.example.shortenurl.repository.mapper.UserRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String USER_COLUMNS = """
            id, email, password_hash, created_at
            """;

    private static final String EMAIL = "email";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRowMapper rowMapper;

    public void save(User user) {
        String sql = """
                INSERT INTO users (email, password_hash)
                VALUES (:email, :passwordHash)
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(EMAIL, user.email())
                .addValue("passwordHash", user.passwordHash());

        jdbcTemplate.update(sql, parameters);
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT %s
                FROM users
                WHERE email = :email
                """.formatted(USER_COLUMNS);

        return firstOrEmpty(jdbcTemplate.query(sql, Map.of(EMAIL, email), rowMapper));
    }

    public boolean existsByEmail(String email) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE email = :email
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Map.of(EMAIL, email),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    private Optional<User> firstOrEmpty(List<User> users) {
        return users.stream().findFirst();
    }
}
