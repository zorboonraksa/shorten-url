package org.example.shortenurl.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record User(Long id, String email, String passwordHash, Instant createdAt) {

}
