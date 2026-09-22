package com.vikrant.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.vikrant.urlshortener.config.AppProperties;
import com.vikrant.urlshortener.dto.UrlAnalyticsResponse;
import com.vikrant.urlshortener.entity.ClickEvent;
import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.repository.ClickEventRepository;
import com.vikrant.urlshortener.repository.UrlRepository;
import com.vikrant.urlshortener.Services.impl.UrlShortenerServiceImpl;
import com.vikrant.urlshortener.exception.ShortUrlExpiredException;
import com.vikrant.urlshortener.exception.InvalidExpirationException;
import com.vikrant.urlshortener.Services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private AppProperties appProperties;
    @Mock
    private ClickEventRepository clickEventRepository;
    @InjectMocks
    private UrlShortenerServiceImpl service;

    @Test
    void shouldCreateShortUrlForNewUrl() {
        // Arrange
        String originalUrl = "https://google.com";
        String generatedCode = "x7Kp91a";
        String baseUrl = "http://localhost:8080/";

        when(appProperties.getBaseUrl())
                .thenReturn(baseUrl);


        when(urlRepository.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.empty());

        when(codeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.findByShortCode(generatedCode))
                .thenReturn(Optional.empty());


        //Act
        String result = service.shortUrl(originalUrl,null);

        //Assert
        assertEquals(
                baseUrl + generatedCode,
                result
        );

        verify(urlRepository).save(any(Url.class));
    }
    @Test
    void shouldReturnExistingShortUrlForDuplicateUrl() {
        //Arrange
        String originalUrl = "https://google.com";
        String baseUrl = "http://localhost:8080/";
        Url existingUrl = new Url();

        existingUrl.setOriginalUrl(originalUrl);
        existingUrl.setShortCode("abc1234");

        when(appProperties.getBaseUrl())
                .thenReturn(baseUrl);

        when(urlRepository.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.of(existingUrl));

        //Act
        String result = service.shortUrl(originalUrl,null);

        //Assert
        assertEquals(
                baseUrl+"abc1234",
                result
        );

        verify(codeGenerator, never()).generate();

        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    void shouldReturnOriginalUrl() {

        String code = "abc1234";
        String originalUrl = "https://google.com";

        Url url = new Url();

        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        String result = service.getOriginalUrl(code);
        //assert we implement verifications
        assertEquals(originalUrl, result);
        verify(clickEventRepository)
                .save(any(ClickEvent.class));
    }
    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {

        String code = "doesNotExist";

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> service.getOriginalUrl(code)
        );
    }
    @Test
    void shouldRejectExpiredUrl() {

        Url url = new Url();

        url.setOriginalUrl("https://example.com");
        url.setShortCode("abc123");
        url.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(urlRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(url));

        assertThatThrownBy(() ->
                service.getOriginalUrl("abc123")
        )
                .isInstanceOf(ShortUrlExpiredException.class);

        verify(clickEventRepository,never())
                .save(any(ClickEvent.class));

    }
    @Test
    void shouldRejectPastExpirationDate() {

        LocalDateTime past =
                LocalDateTime.now().minusMinutes(10);

        assertThatThrownBy(() ->
                service.shortUrl(
                        "https://example.com",
                        past
                )
        )
                .isInstanceOf(InvalidExpirationException.class)
                .hasMessage("Expiration time must be in the future");
    }
    @Test
    void shouldReturnUrlAnalytics() {

        Long urlId = 1L;
        String code = "abc123";
        String originalUrl = "https://google.com";

        LocalDateTime firstClick =
                LocalDateTime.of(2026, 9, 22, 10, 0);

        LocalDateTime lastClick =
                LocalDateTime.of(2026, 9, 22, 11, 0);

        Url url = new Url();
        url.setId(urlId);
        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);

        ClickEvent firstEvent = new ClickEvent();
        firstEvent.setUrl(url);
        firstEvent.setClickedAt(firstClick);

        ClickEvent lastEvent = new ClickEvent();
        lastEvent.setUrl(url);
        lastEvent.setClickedAt(lastClick);

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        when(clickEventRepository.countByUrlId(urlId))
                .thenReturn(2L);

        when(clickEventRepository
                .findFirstByUrlIdOrderByClickedAtAsc(urlId))
                .thenReturn(Optional.of(firstEvent));

        when(clickEventRepository
                .findFirstByUrlIdOrderByClickedAtDesc(urlId))
                .thenReturn(Optional.of(lastEvent));

        // Act
        UrlAnalyticsResponse result =
                service.getAnalytics(code);

        // Assert
        assertEquals(code, result.getShortCode());

        assertEquals(
                originalUrl,
                result.getOriginalUrl()
        );

        assertEquals(
                2L,
                result.getTotalClicks()
        );

        assertEquals(
                firstClick,
                result.getFirstClickedAt()
        );

        assertEquals(
                lastClick,
                result.getLastClickedAt()
        );
        // Because Url.id has no public setter,
        // Mockito can return the URL but the ID must be available
        // for countByUrlId().
    }
    @Test
    void shouldReturnAnalyticsWithZeroClicks() {

        Long urlId = 1L;
        String code = "abc123";
        String originalUrl = "https://google.com";

        Url url = new Url();

        url.setId(urlId);
        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        when(clickEventRepository.countByUrlId(urlId))
                .thenReturn(0L);

        when(clickEventRepository
                .findFirstByUrlIdOrderByClickedAtAsc(urlId))
                .thenReturn(Optional.empty());

        when(clickEventRepository
                .findFirstByUrlIdOrderByClickedAtDesc(urlId))
                .thenReturn(Optional.empty());

        // Act
        UrlAnalyticsResponse result =
                service.getAnalytics(code);

        // Assert
        assertEquals(code, result.getShortCode());
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(0L, result.getTotalClicks());
        assertNull(result.getFirstClickedAt());
        assertNull(result.getLastClickedAt());
    }


    // is test is for not found behaviour of the url in cliked one

    @Test
    void shouldThrowExceptionWhenAnalyticsCodeDoesNotExist() {

        String code = "doesNotExist";

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> service.getAnalytics(code)
        );

        verifyNoInteractions(clickEventRepository);
    }
}