package org.example.shortenurl.dtos;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UrlResponse(
        Long id,
        @JsonProperty("original_url")
        String originalUrl,
        @JsonProperty("short_url")
        String shortUrl,
        @JsonProperty("created_at")
        Instant createdAt
) {
}
