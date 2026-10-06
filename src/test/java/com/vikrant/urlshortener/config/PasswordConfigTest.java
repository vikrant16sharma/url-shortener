package com.vikrant.urlshortener.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PasswordConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldEncodePassword() {

        String rawPassword = "password123";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword)
                .isNotEqualTo(rawPassword);

        assertThat(encodedPassword)
                .isNotBlank();
    }

    @Test
    void shouldMatchCorrectPassword() {

        String rawPassword = "password123";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        boolean matches =
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword
                );

        assertThat(matches).isTrue();
    }

    @Test
    void shouldRejectIncorrectPassword() {

        String rawPassword = "password123";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        boolean matches =
                passwordEncoder.matches(
                        "wrongpassword",
                        encodedPassword
                );

        assertThat(matches).isFalse();
    }
}