package com.vikrant.urlshortener.controller;

import com.vikrant.urlshortener.exception.GlobalExceptionHandler;
import com.vikrant.urlshortener.exception.ShortUrlNotFoundException;
import com.vikrant.urlshortener.Services.UrlShortenerService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.context.annotation.Import;
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

        when(service.shortUrl("https://google.com"))
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
}