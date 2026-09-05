package org.example.shortenurl.controller;

import org.example.shortenurl.dtos.ShortenUrlRequest;
import org.example.shortenurl.dtos.ShortenUrlResponse;
import org.example.shortenurl.dtos.UrlResponse;
import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.service.ShortenedUrlService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlsControllerTest {

    @Mock
    private ShortenedUrlService shortenedUrlService;

    @BeforeEach
    void setRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request, new MockHttpServletResponse())
        );
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shortenReturnsCreatedShortUrl() {
        UrlsController controller = new UrlsController(shortenedUrlService);
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com");
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .shortCode("abc1234")
                .build();
        when(shortenedUrlService.shorten(7L, request.originalUrl())).thenReturn(shortenedUrl);

        ResponseEntity<ShortenUrlResponse> response = controller.shorten(request, jwt());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("http://localhost:8080/r/abc1234", response.getBody().shortUrl());
    }

    @Test
    void getUrlsMapsRepositoryModelsToResponses() {
        UrlsController controller = new UrlsController(shortenedUrlService);
        Instant createdAt = Instant.parse("2026-09-05T00:00:00Z");
        when(shortenedUrlService.findAllByUserId(7L)).thenReturn(List.of(
                ShortenedUrl.builder()
                        .id(10L)
                        .userId(7L)
                        .originalUrl("https://example.com/path")
                        .shortCode("abc1234")
                        .createdAt(createdAt)
                        .build()
        ));

        List<UrlResponse> response = controller.getUrls(jwt());

        assertEquals(List.of(new UrlResponse(
                10L,
                "https://example.com/path",
                "http://localhost:8080/r/abc1234",
                createdAt
        )), response);
    }

    @Test
    void deleteReturnsNoContent() {
        UrlsController controller = new UrlsController(shortenedUrlService);

        ResponseEntity<Void> response = controller.deleteUrl(10L, jwt());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(shortenedUrlService).delete(10L, 7L);
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("7")
                .build();
    }
}
