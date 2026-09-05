package org.example.shortenurl.controller;

import org.example.shortenurl.service.ShortenedUrlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private ShortenedUrlService shortenedUrlService;

    @Test
    void redirectReturnsLocation() {
        RedirectController controller = new RedirectController(shortenedUrlService);
        URI originalUrl = URI.create("https://example.com/path");
        when(shortenedUrlService.getRedirectUri("abc1234")).thenReturn(originalUrl);

        ResponseEntity<Void> response = controller.redirect("abc1234");

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(originalUrl, response.getHeaders().getLocation());
        assertNull(response.getBody());
    }
}
