package org.example.shortenurl.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shortenurl.dtos.ShortenUrlRequest;
import org.example.shortenurl.dtos.ShortenUrlResponse;
import org.example.shortenurl.dtos.UrlResponse;
import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.service.ShortenedUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UrlsController {

    private final ShortenedUrlService shortenedUrlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shorten(
            @Valid @RequestBody ShortenUrlRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = userId(jwt);
        logRequestStarted("shorten", userId);
        ShortenedUrl shortenedUrl = shortenedUrlService.shorten(
                userId,
                request.originalUrl()
        );

        logRequestCompleted("shorten", userId, HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortenUrlResponse(shortUrl(shortenedUrl.shortCode())));
    }

    @GetMapping("/urls")
    public List<UrlResponse> getUrls(@AuthenticationPrincipal Jwt jwt) {
        Long userId = userId(jwt);
        logRequestStarted("list", userId);
        List<UrlResponse> urls = shortenedUrlService.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
        logRequestCompleted("list", userId, HttpStatus.OK);
        return urls;
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = userId(jwt);
        logRequestStarted("delete", userId);
        shortenedUrlService.delete(id, userId);
        logRequestCompleted("delete", userId, HttpStatus.NO_CONTENT);
        return ResponseEntity.noContent().build();
    }

    private UrlResponse toResponse(ShortenedUrl shortenedUrl) {
        return new UrlResponse(
                shortenedUrl.id(),
                shortenedUrl.originalUrl(),
                shortUrl(shortenedUrl.shortCode()),
                shortenedUrl.createdAt()
        );
    }

    private String shortUrl(String shortCode) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/r/{shortCode}")
                .buildAndExpand(shortCode)
                .toUriString();
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(
                jwt.getSubject(),
                "JWT subject is required"
        ));
    }

    private void logRequestStarted(String operation, Long userId) {
        log.info("URL request started operation={} userId={}", operation, userId);
    }

    private void logRequestCompleted(String operation, Long userId, HttpStatus status) {
        log.info(
                "URL request completed operation={} userId={} status={}",
                operation,
                userId,
                status.value()
        );
    }
}
