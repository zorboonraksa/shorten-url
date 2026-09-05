package org.example.shortenurl.repository.mapper;

import org.example.shortenurl.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .email(resultSet.getString("email"))
                .passwordHash(resultSet.getString("password_hash"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .build();
    }
}
