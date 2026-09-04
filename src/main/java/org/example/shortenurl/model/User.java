package org.example.shortenurl.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record User(Long id, String email, String passwordHash, Instant createdAt) {

}
