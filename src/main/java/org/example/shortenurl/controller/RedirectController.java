package org.example.shortenurl.controller;

import java.net.URI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shortenurl.service.ShortenedUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final ShortenedUrlService shortenedUrlService;

    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        log.info("Redirect request started");
        URI originalUrl = shortenedUrlService.getRedirectUri(shortCode);
        log.info("Redirect request completed status={}", HttpStatus.FOUND.value());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(originalUrl)
                .build();
    }
}
