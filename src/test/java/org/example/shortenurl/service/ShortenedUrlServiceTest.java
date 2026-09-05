package org.example.shortenurl.service;

import org.example.shortenurl.exception.ApiException;
import org.example.shortenurl.model.ShortenedUrl;
import org.example.shortenurl.repository.ShortenedUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortenedUrlServiceTest {

    @Mock
    private ShortenedUrlRepository shortenedUrlRepository;

    @InjectMocks
    private ShortenedUrlService shortenedUrlService;

    @Test
    void shortenValidatesAndSavesUrl() {
        when(shortenedUrlRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ShortenedUrl result = shortenedUrlService.shorten(7L, " https://example.com/path ");

        assertEquals(7L, result.userId());
        assertEquals("https://example.com/path", result.originalUrl());
        assertTrue(result.shortCode().matches("[A-Za-z0-9]{7}"));
    }

    @Test
    void shortenAcceptsHttpUrl() {
        when(shortenedUrlRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ShortenedUrl result = shortenedUrlService.shorten(7L, "http://example.com/path");

        assertEquals("http://example.com/path", result.originalUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "example.com/path",
            "//example.com/path",
            "https://",
            "ftp://example.com/file"
    })
    void shortenRejectsInvalidOrUnsupportedUrl(String originalUrl) {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> shortenedUrlService.shorten(7L, originalUrl)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("original_url must be a valid HTTP or HTTPS URL", exception.getMessage());
        verifyNoInteractions(shortenedUrlRepository);
    }

    @Test
    void shortenRetriesWhenConcurrentInsertUsesSameCode() {
        when(shortenedUrlRepository.save(any(ShortenedUrl.class)))
                .thenThrow(new DuplicateKeyException("duplicate short code"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortenedUrl result = shortenedUrlService.shorten(7L, "https://example.com");

        assertEquals("https://example.com", result.originalUrl());
        verify(shortenedUrlRepository, times(2)).save(any(ShortenedUrl.class));
    }

    @Test
    void shortenReturnsServerErrorAfterAllCodesCollide() {
        when(shortenedUrlRepository.save(any(ShortenedUrl.class)))
                .thenThrow(new DuplicateKeyException("duplicate short code"));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> shortenedUrlService.shorten(7L, "https://example.com")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Unable to generate short URL", exception.getMessage());
        verify(shortenedUrlRepository, times(5)).save(any(ShortenedUrl.class));
    }

    @Test
    void getRedirectUriReturnsOriginalUrl() {
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .originalUrl("https://example.com/path")
                .shortCode("abc1234")
                .build();
        when(shortenedUrlRepository.findByShortCode("abc1234"))
                .thenReturn(Optional.of(shortenedUrl));

        URI result = shortenedUrlService.getRedirectUri("abc1234");

        assertEquals(URI.create("https://example.com/path"), result);
    }

    @Test
    void getRedirectUriReturnsNotFoundForUnknownCode() {
        when(shortenedUrlRepository.findByShortCode("missing"))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> shortenedUrlService.getRedirectUri("missing")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Short URL not found", exception.getMessage());
    }

    @Test
    void findAllByUserIdReturnsOnlyRepositoryResultForThatUser() {
        List<ShortenedUrl> expected = List.of(
                ShortenedUrl.builder().id(1L).userId(7L).build(),
                ShortenedUrl.builder().id(2L).userId(7L).build()
        );
        when(shortenedUrlRepository.findAllByUserId(7L)).thenReturn(expected);

        List<ShortenedUrl> result = shortenedUrlService.findAllByUserId(7L);

        assertSame(expected, result);
        verify(shortenedUrlRepository).findAllByUserId(7L);
    }

    @Test
    void deleteSucceedsWhenUrlBelongsToUser() {
        when(shortenedUrlRepository.deleteByIdAndUserId(10L, 7L)).thenReturn(1);

        shortenedUrlService.delete(10L, 7L);

        verify(shortenedUrlRepository).deleteByIdAndUserId(10L, 7L);
    }

    @Test
    void deleteReturnsNotFoundWhenUrlDoesNotBelongToUser() {
        when(shortenedUrlRepository.deleteByIdAndUserId(10L, 7L)).thenReturn(0);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> shortenedUrlService.delete(10L, 7L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Short URL not found", exception.getMessage());
    }
}
