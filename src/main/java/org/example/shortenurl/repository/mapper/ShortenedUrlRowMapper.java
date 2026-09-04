package org.example.shortenurl.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.shortenurl.model.ShortenedUrl;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ShortenedUrlRowMapper implements RowMapper<ShortenedUrl> {

    @Override
    public ShortenedUrl mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return ShortenedUrl.builder()
                .id(resultSet.getLong("id"))
                .userId(resultSet.getLong("user_id"))
                .originalUrl(resultSet.getString("original_url"))
                .shortCode(resultSet.getString("short_code"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .build();
    }
}
