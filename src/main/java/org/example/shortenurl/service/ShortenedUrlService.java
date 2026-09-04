package org.example.shortenurl.service;

import java.net.URI;
import java.security.SecureRandom;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.example.shortenurl.exception.ApiException;
import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.repository.ShortenedUrlRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortenedUrlService {

    private static final String CODE_CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShortenedUrlRepository shortenedUrlRepository;

    public ShortenedUrl shorten(Long userId, String requestedUrl) {
        String originalUrl = requestedUrl.trim();
        validateUrl(originalUrl);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String shortCode = generateShortCode();
            if (shortenedUrlRepository.existsByShortCode(shortCode)) {
                continue;
            }

            ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                    .userId(userId)
                    .originalUrl(originalUrl)
                    .shortCode(shortCode)
                    .build();

            try {
                return shortenedUrlRepository.save(shortenedUrl);
            } catch (DuplicateKeyException exception) {
                // A concurrent request used the same code; generate another one.
            }
        }

        throw new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to generate short URL"
        );
    }

    public URI getRedirectUri(String shortCode) {
        return shortenedUrlRepository.findByShortCode(shortCode)
                .map(ShortenedUrl::originalUrl)
                .map(URI::create)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Short URL not found"
                ));
    }

    public List<ShortenedUrl> findAllByUserId(Long userId) {
        return shortenedUrlRepository.findAllByUserId(userId);
    }

    public void delete(Long id, Long userId) {
        if (shortenedUrlRepository.deleteByIdAndUserId(id, userId) == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Short URL not found"
            );
        }
    }

    private void validateUrl(String originalUrl) {
        try {
            URI uri = URI.create(originalUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() == null
                    || scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw invalidUrl();
            }
        } catch (IllegalArgumentException exception) {
            throw invalidUrl();
        }
    }

    private String generateShortCode() {
        StringBuilder shortCode = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            shortCode.append(CODE_CHARACTERS.charAt(
                    RANDOM.nextInt(CODE_CHARACTERS.length())
            ));
        }
        return shortCode.toString();
    }

    private ApiException invalidUrl() {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                "original_url must be a valid HTTP or HTTPS URL"
        );
    }
}
