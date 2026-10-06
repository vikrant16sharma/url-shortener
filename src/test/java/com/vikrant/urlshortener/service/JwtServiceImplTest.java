package com.vikrant.urlshortener.service;

import com.vikrant.urlshortener.config.JwtProperties;
import com.vikrant.urlshortener.Services.impl.JwtServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {

        JwtProperties properties =
                new JwtProperties();

        properties.setSecret(
                "this-is-a-very-long-development-secret-key-123456789"
        );

        properties.setExpirationMs(
                3600000
        );

        jwtService =
                new JwtServiceImpl(properties);
    }

    @Test
    void shouldGenerateJwtToken() {

        String token =
                jwtService.generateToken(
                        1L,
                        "test@example.com"
                );

        assertThat(token)
                .isNotBlank();

        assertThat(token.split("\\."))
                .hasSize(3);
    }
}