package org.example.shortenurl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShortenUrlResponse(
        @JsonProperty("short_url") String shortUrl
) {
}
