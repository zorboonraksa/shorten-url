package org.example.shortenurl.repository;

import org.example.shortenurl.model.User;
import org.example.shortenurl.repository.mapper.UserRowMapper;
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
class UserRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private UserRowMapper rowMapper;

    @InjectMocks
    private UserRepository repository;

    @Test
    void saveUsesEmailAndPasswordHash() {
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("password-hash")
                .build();
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        repository.save(user);

        verify(jdbcTemplate).update(anyString(), parameters.capture());
        assertSame("user@example.com", parameters.getValue().getValue("email"));
        assertSame("password-hash", parameters.getValue().getValue("passwordHash"));
    }

    @Test
    void findByEmailReturnsFirstUser() {
        User expected = User.builder().id(7L).build();
        when(jdbcTemplate.query(anyString(), eq(Map.of("email", "user@example.com")), eq(rowMapper)))
                .thenReturn(List.of(expected));

        Optional<User> result = repository.findByEmail("user@example.com");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
    }

    @Test
    void findByEmailReturnsEmptyWhenNoUserExists() {
        when(jdbcTemplate.query(anyString(), anyMap(), eq(rowMapper))).thenReturn(List.of());

        assertTrue(repository.findByEmail("missing@example.com").isEmpty());
    }

    @Test
    void existsByEmailMapsTrueAndNullSafely() {
        when(jdbcTemplate.queryForObject(anyString(), anyMap(), eq(Boolean.class)))
                .thenReturn(true, (Boolean) null);

        assertTrue(repository.existsByEmail("user@example.com"));
        assertFalse(repository.existsByEmail("missing@example.com"));
    }
}
