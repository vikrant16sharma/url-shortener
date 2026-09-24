package com.vikrant.urlshortener.controller;

import com.vikrant.urlshortener.exception.GlobalExceptionHandler;
import com.vikrant.urlshortener.exception.InvalidExpirationException;
import com.vikrant.urlshortener.exception.ShortUrlExpiredException;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.Services.UrlShortenerService;
import com.vikrant.urlshortener.dto.UrlAnalyticsResponse;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.Import;
import static org.hamcrest.Matchers.nullValue;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UrlController.class)
@Import(GlobalExceptionHandler.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService service;

    @Test
    void shouldCreateShortUrl() throws Exception {

        when(service.shortUrl("https://google.com",null))
                .thenReturn("short.ly/x7Kp91a");

        mockMvc.perform(
                        post("/api/urls")
                                .contentType("application/json")
                                .content("""
                            {
                                "url": "https://google.com"
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl")
                        .value("short.ly/x7Kp91a"));
    }

    @Test
    void shouldRedirectToOriginalUrl() throws Exception {

        when(service.getOriginalUrl("x7Kp91a"))
                .thenReturn("https://google.com");

        mockMvc.perform(
                        get("/x7Kp91a")
                )
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location",
                        "https://google.com"
                ));
    }

    @Test
    void shouldReturn404WhenShortCodeDoesNotExist() throws Exception {

        when(service.getOriginalUrl("doesNotExist"))
                .thenThrow(
                        new ShortUrlNotFoundException(
                                "Short URL not found: doesNotExist"
                        )
                );

        mockMvc.perform(
                        get("/doesNotExist")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForInvalidUrl() throws Exception {

        mockMvc.perform(
                        post("/api/urls")
                                .contentType("application/json")
                                .content("""
                            {
                                "url": "google.com"
                            }
                            """)
                )
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn410WhenShortUrlIsExpired() throws Exception {

        when(service.getOriginalUrl("abc123"))
                .thenThrow(
                        new ShortUrlExpiredException(
                                "Short Url has expired: abc123"
                        )
                );

        mockMvc.perform(get("/abc123")).andExpect(status().isGone());
    }
    @Test
    void shouldReturn400ForInvalidExpiration() throws Exception {

        when(service.shortUrl(
                "https://example.com",
                LocalDateTime.parse("2020-01-01T10:00:00")
        )).thenThrow(
                new InvalidExpirationException(
                        "Expiration time must be in the future"
                )
        );

        mockMvc.perform(
                        post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "url": "https://example.com",
                            "expiresAt": "2020-01-01T10:00:00"
                        }
                        """)
                )
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturnUrlAnalytics() throws Exception {

        LocalDateTime firstClick =
                LocalDateTime.of(2026, 9, 22, 10, 0);

        LocalDateTime lastClick =
                LocalDateTime.of(2026, 9, 22, 11, 0);

        UrlAnalyticsResponse response =
                new UrlAnalyticsResponse(
                        "abc123",
                        "https://google.com",
                        5L,
                        firstClick,
                        lastClick
                );

        when(service.getAnalytics("abc123"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/urls/abc123/analytics")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode")
                        .value("abc123"))
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://google.com"))
                .andExpect(jsonPath("$.totalClicks")
                        .value(5))
                .andExpect(jsonPath("$.firstClickedAt")
                        .value("2026-09-22T10:00:00"))
                .andExpect(jsonPath("$.lastClickedAt")
                        .value("2026-09-22T11:00:00"));
    }

    @Test
    void shouldReturn404WhenAnalyticsCodeDoesNotExist() throws Exception {

        when(service.getAnalytics("doesNotExist"))
                .thenThrow(
                        new ShortUrlNotFoundException(
                                "Short URL not found: doesNotExist"
                        )
                );

        mockMvc.perform(
                        get("/api/urls/doesNotExist/analytics")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAnalyticsWithZeroClicks() throws Exception {

        UrlAnalyticsResponse response =
                new UrlAnalyticsResponse(
                        "abc123",
                        "https://google.com",
                        0L,
                        null,
                        null
                );

        when(service.getAnalytics("abc123"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/urls/abc123/analytics")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode")
                        .value("abc123"))
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://google.com"))
                .andExpect(jsonPath("$.totalClicks")
                        .value(0))
                .andExpect(jsonPath("$.firstClickedAt")
                        .doesNotExist())
                .andExpect(jsonPath("$.lastClickedAt")
                        .doesNotExist());
    }
}