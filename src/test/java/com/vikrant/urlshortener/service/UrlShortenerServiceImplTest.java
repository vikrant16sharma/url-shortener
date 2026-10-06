package com.vikrant.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.vikrant.urlshortener.config.AppProperties;
import com.vikrant.urlshortener.dto.UrlAnalyticsResponse;
import com.vikrant.urlshortener.entity.ClickEvent;
import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.exception.UrlAccessDeniedException;
import com.vikrant.urlshortener.repository.ClickEventRepository;
import com.vikrant.urlshortener.repository.UrlRepository;
import com.vikrant.urlshortener.Services.impl.UrlShortenerServiceImpl;
import com.vikrant.urlshortener.exception.ShortUrlExpiredException;
import com.vikrant.urlshortener.exception.InvalidExpirationException;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.security.AuthenticatedUserService;
import com.vikrant.urlshortener.Services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
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

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private UrlShortenerServiceImpl service;


    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        return user;
    }
    @Test
    void shouldCreateShortUrlForNewUrl() {
        // Arrange
        String originalUrl = "https://google.com";
        String generatedCode = "x7Kp91a";
        String baseUrl = "http://localhost:8080/";

        User user = createTestUser();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(user);

        when(appProperties.getBaseUrl())
                .thenReturn(baseUrl);

        when(urlRepository.findByOriginalUrl(
                originalUrl,
                user.getId()
        )).thenReturn(Optional.empty());

        when(codeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.findByShortCode(generatedCode))
                .thenReturn(Optional.empty());

        // Act
        String result = service.shortUrl(originalUrl, null);

        // Assert
        assertEquals(
                baseUrl + generatedCode,
                result
        );

        ArgumentCaptor<Url> captor =
                ArgumentCaptor.forClass(Url.class);

        verify(urlRepository).save(captor.capture());

        Url savedUrl = captor.getValue();

        assertEquals(originalUrl, savedUrl.getOriginalUrl());
        assertEquals(generatedCode, savedUrl.getShortCode());
        assertEquals(user, savedUrl.getUser());
    }
    @Test
    void shouldReturnExistingShortUrlForDuplicateUrl() {
        // Arrange
        String originalUrl = "https://google.com";
        String baseUrl = "http://localhost:8080/";

        User user = createTestUser();

        Url existingUrl = new Url();
        existingUrl.setOriginalUrl(originalUrl);
        existingUrl.setShortCode("abc1234");
        existingUrl.setUser(user);

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(user);

        when(appProperties.getBaseUrl())
                .thenReturn(baseUrl);

        when(urlRepository.findByOriginalUrl(
                originalUrl,
                user.getId()
        )).thenReturn(Optional.of(existingUrl));

        // Act
        String result = service.shortUrl(originalUrl, null);

        // Assert
        assertEquals(
                baseUrl + "abc1234",
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

        User user = createTestUser();

        LocalDateTime firstClick =
                LocalDateTime.of(2026, 9, 22, 10, 0);

        LocalDateTime lastClick =
                LocalDateTime.of(2026, 9, 22, 11, 0);

        Url url = new Url();

        url.setId(urlId);
        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);
        url.setUser(user);

        ClickEvent firstEvent = new ClickEvent();
        firstEvent.setUrl(url);
        firstEvent.setClickedAt(firstClick);

        ClickEvent lastEvent = new ClickEvent();
        lastEvent.setUrl(url);
        lastEvent.setClickedAt(lastClick);

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(user);

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

        UrlAnalyticsResponse result =
                service.getAnalytics(code);

        assertEquals(code, result.getShortCode());
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(2L, result.getTotalClicks());
        assertEquals(firstClick, result.getFirstClickedAt());
        assertEquals(lastClick, result.getLastClickedAt());
    }
    @Test
    void shouldReturnAnalyticsWithZeroClicks() {

        Long urlId = 1L;
        String code = "abc123";
        String originalUrl = "https://google.com";

        User user = createTestUser();

        Url url = new Url();

        url.setId(urlId);
        url.setOriginalUrl(originalUrl);
        url.setShortCode(code);
        url.setUser(user);

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(user);

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
    @Test
    void shouldCreateSeparateShortUrlForDifferentUser() {
        // Arrange
        String originalUrl = "https://google.com";

        User user1 = createTestUser();

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(user2);

        when(urlRepository.findByOriginalUrl(
                originalUrl,
                user2.getId()
        )).thenReturn(Optional.empty());

        when(codeGenerator.generate())
                .thenReturn("xyz789");

        when(urlRepository.findByShortCode("xyz789"))
                .thenReturn(Optional.empty());

        when(appProperties.getBaseUrl())
                .thenReturn("http://localhost:8080/");

        // Act
        String result =
                service.shortUrl(originalUrl, null);

        // Assert
        assertEquals(
                "http://localhost:8080/xyz789",
                result
        );

        ArgumentCaptor<Url> captor =
                ArgumentCaptor.forClass(Url.class);

        verify(urlRepository).save(captor.capture());

        assertEquals(
                user2,
                captor.getValue().getUser()
        );
    }
    @Test
    void shouldRejectAnalyticsAccessForDifferentUser() {

        User owner = createTestUser();

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("other@example.com");

        Url url = new Url();
        url.setId(1L);
        url.setOriginalUrl("https://google.com");
        url.setShortCode("abc123");
        url.setUser(owner);

        when(urlRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(url));

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(differentUser);

        assertThrows(
                UrlAccessDeniedException.class,
                () -> service.getAnalytics("abc123")
        );

        verifyNoInteractions(clickEventRepository);
    }
}