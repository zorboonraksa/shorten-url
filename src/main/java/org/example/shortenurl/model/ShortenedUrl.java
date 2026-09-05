package org.example.shortenurl.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ShortenedUrl(Long id, Long userId, String originalUrl, String shortCode, Instant createdAt) {

}
