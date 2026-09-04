package org.example.shortenurl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ShortenUrlRequest(
        @JsonProperty("original_url")
        @NotBlank(message = "original_url must not be blank")
        String originalUrl
) {
}
