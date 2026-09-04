package org.example.shortenurl.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record ShortenedUrl(Long id, Long userId, String originalUrl, String shortCode, Instant createdAt) {

}
