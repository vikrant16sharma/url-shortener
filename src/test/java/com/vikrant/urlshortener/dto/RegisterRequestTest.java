package com.vikrant.urlshortener.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidRegistrationRequest() {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("test@example.com");
        request.setPassword("password123");

        assertThat(
                validator.validate(request)
        ).isEmpty();
    }

    @Test
    void shouldRejectBlankEmail() {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("");
        request.setPassword("password123");

        assertThat(
                validator.validate(request)
        ).isNotEmpty();
    }

    @Test
    void shouldRejectInvalidEmail() {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("invalid-email");
        request.setPassword("password123");

        assertThat(
                validator.validate(request)
        ).isNotEmpty();
    }

    @Test
    void shouldRejectShortPassword() {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("test@example.com");
        request.setPassword("1234567");

        assertThat(
                validator.validate(request)
        ).isNotEmpty();
    }

    @Test
    void shouldRejectBlankPassword() {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("test@example.com");
        request.setPassword("");

        assertThat(
                validator.validate(request)
        ).isNotEmpty();
    }
}