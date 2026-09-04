package org.example.shortenurl.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shortenurl.dtos.ShortenUrlRequest;
import org.example.shortenurl.dtos.ShortenUrlResponse;
import org.example.shortenurl.dtos.UrlResponse;
import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.service.ShortenedUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlsController {

    private final ShortenedUrlService shortenedUrlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shorten(
            @Valid @RequestBody ShortenUrlRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ShortenedUrl shortenedUrl = shortenedUrlService.shorten(
                userId(jwt),
                request.originalUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortenUrlResponse(shortUrl(shortenedUrl.shortCode())));
    }

    @GetMapping("/urls")
    public List<UrlResponse> getUrls(@AuthenticationPrincipal Jwt jwt) {
        return shortenedUrlService.findAllByUserId(userId(jwt)).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        shortenedUrlService.delete(id, userId(jwt));
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
        return Long.valueOf(jwt.getSubject());
    }
}
