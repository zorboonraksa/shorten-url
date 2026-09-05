package org.example.shortenurl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

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
