package com.vikrant.urlshortener.service;

import com.vikrant.urlshortener.Services.JwtService;
import com.vikrant.urlshortener.dto.RegisterRequest;
import com.vikrant.urlshortener.dto.RegisterResponse;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.exception.EmailAlreadyRegisteredException;
import com.vikrant.urlshortener.repository.UserRepository;
import com.vikrant.urlshortener.Services.impl.AuthServiceImpl;
import com.vikrant.urlshortener.dto.LoginRequest;
import com.vikrant.urlshortener.dto.LoginResponse;
import com.vikrant.urlshortener.exception.InvalidCredentialsException;


import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService =
                new AuthServiceImpl(
                        userRepository,
                        passwordEncoder,
                        jwtService
                );
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request =
                new RegisterRequest();

        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(
                "test@example.com"
        )).thenReturn(false);

        when(passwordEncoder.encode(
                "password123"
        )).thenReturn("hashed-password");

        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response =
                authService.register(request);

        assertThat(response.getId())
                .isEqualTo(1L);

        assertThat(response.getEmail())
                .isEqualTo("test@example.com");

        verify(userRepository)
                .existsByEmail("test@example.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldRejectAlreadyRegisteredEmail() {

        RegisterRequest request =
                new RegisterRequest();

        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(
                "test@example.com"
        )).thenReturn(true);

        assertThatThrownBy(
                () -> authService.register(request)
        )
                .isInstanceOf(EmailAlreadyRegisteredException.class)
                .hasMessage("Email is already registered");

        verify(userRepository)
                .existsByEmail("test@example.com");

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void shouldStoreEncodedPassword() {

        RegisterRequest request =
                new RegisterRequest();

        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(
                "test@example.com"
        )).thenReturn(false);

        when(passwordEncoder.encode(
                "password123"
        )).thenReturn("hashed-password");

        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    assertThat(user.getPasswordHash())
                            .isEqualTo("hashed-password");

                    assertThat(user.getPasswordHash())
                            .isNotEqualTo("password123");

                    return savedUser;
                });

        authService.register(request);

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request =
                new LoginRequest();

        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();

        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashed-password"
        )).thenReturn(true);
        when(jwtService.generateToken(
                1L,
                "test@example.com"
        )).thenReturn("test-jwt-token");

        LoginResponse response =
                authService.login(request);

        assertThat(response.getId())
                .isEqualTo(1L);

        assertThat(response.getEmail())
                .isEqualTo("test@example.com");

        assertThat(response.getAccessToken())
                .isEqualTo("test-jwt-token");


        verify(userRepository)
                .findByEmail("test@example.com");

        verify(passwordEncoder)
                .matches(
                        "password123",
                        "hashed-password"
                );
        verify(jwtService)
                .generateToken(
                        1L,
                        "test@example.com"
                );
    }

    @Test
    void shouldRejectUnknownEmail() {

        LoginRequest request =
                new LoginRequest();

        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(
                "unknown@example.com"
        )).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> authService.login(request)
        )
                .isInstanceOf(
                        InvalidCredentialsException.class
                )
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never())
                .matches(any(), any());
    }

    @Test
    void shouldRejectWrongPassword() {

        LoginRequest request =
                new LoginRequest();

        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();

        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongpassword",
                "hashed-password"
        )).thenReturn(false);

        assertThatThrownBy(
                () -> authService.login(request)
        )
                .isInstanceOf(
                        InvalidCredentialsException.class
                )
                .hasMessage("Invalid email or password");
    }
}