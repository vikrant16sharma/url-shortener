package com.vikrant.urlshortener.service;

import com.vikrant.urlshortener.dto.*;
import com.vikrant.urlshortener.exception.*;

import com.vikrant.urlshortener.Services.JwtService;
import com.vikrant.urlshortener.Services.RefreshTokenService;
import com.vikrant.urlshortener.dto.RegisterRequest;
import com.vikrant.urlshortener.dto.RegisterResponse;
import com.vikrant.urlshortener.entity.RefreshToken;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.exception.EmailAlreadyRegisteredException;
import com.vikrant.urlshortener.repository.UserRepository;
import com.vikrant.urlshortener.Services.impl.AuthServiceImpl;
import com.vikrant.urlshortener.dto.LoginRequest;
import com.vikrant.urlshortener.dto.LoginResponse;
import com.vikrant.urlshortener.exception.InvalidCredentialsException;


import java.time.LocalDateTime;
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

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService =
                new AuthServiceImpl(
                        userRepository,
                        passwordEncoder,
                        jwtService,
                        refreshTokenService
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

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setToken("test-refresh-token");
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                java.time.LocalDateTime.now().plusDays(7)
        );

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

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);

        LoginResponse response =
                authService.login(request);

        assertThat(response.getId())
                .isEqualTo(1L);

        assertThat(response.getEmail())
                .isEqualTo("test@example.com");

        assertThat(response.getAccessToken())
                .isEqualTo("test-jwt-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("test-refresh-token");

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

        verify(refreshTokenService)
                .createRefreshToken(user);
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

        verify(jwtService, never())
                .generateToken(any(), any());

        verify(refreshTokenService, never())
                .createRefreshToken(any(User.class));
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

        verify(jwtService, never())
                .generateToken(any(), any());

        verify(refreshTokenService, never())
                .createRefreshToken(any(User.class));
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("valid-refresh-token");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        // Old refresh token
        RefreshToken oldRefreshToken =
                new RefreshToken();

        oldRefreshToken.setToken("valid-refresh-token");
        oldRefreshToken.setUser(user);
        oldRefreshToken.setRevoked(false);
        oldRefreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        // New refresh token created during rotation
        RefreshToken newRefreshToken =
                new RefreshToken();

        newRefreshToken.setToken("new-refresh-token");
        newRefreshToken.setUser(user);
        newRefreshToken.setRevoked(false);
        newRefreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenService.verifyRefreshToken(
                "valid-refresh-token"
        )).thenReturn(oldRefreshToken);

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(newRefreshToken);

        when(jwtService.generateToken(
                1L,
                "test@example.com"
        )).thenReturn("new-access-token");

        RefreshTokenResponse response =
                authService.refreshAccessToken(request);

        assertThat(response.getAccessToken())
                .isEqualTo("new-access-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("new-refresh-token");

        verify(refreshTokenService)
                .verifyRefreshToken("valid-refresh-token");

        verify(refreshTokenService)
                .revokeToken("valid-refresh-token");

        verify(refreshTokenService)
                .createRefreshToken(user);

        verify(jwtService)
                .generateToken(
                        1L,
                        "test@example.com"
                );
    }

    @Test
    void shouldRejectInvalidRefreshToken() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("invalid-token");

        when(refreshTokenService.verifyRefreshToken(
                "invalid-token"
        )).thenThrow(
                new InvalidRefreshTokenException(
                        "Invalid refresh token"
                )
        );

        assertThatThrownBy(
                () -> authService.refreshAccessToken(request)
        )
                .isInstanceOf(
                        InvalidRefreshTokenException.class
                );

        verify(jwtService, never())
                .generateToken(any(), any());
    }
    @Test
    void shouldRotateRefreshToken() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("old-refresh-token");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        RefreshToken oldToken =
                new RefreshToken();

        oldToken.setToken("old-refresh-token");
        oldToken.setUser(user);
        oldToken.setRevoked(false);
        oldToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        RefreshToken newToken =
                new RefreshToken();

        newToken.setToken("new-refresh-token");
        newToken.setUser(user);
        newToken.setRevoked(false);
        newToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenService.verifyRefreshToken(
                "old-refresh-token"
        )).thenReturn(oldToken);

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(newToken);

        when(jwtService.generateToken(
                1L,
                "test@example.com"
        )).thenReturn("new-access-token");

        RefreshTokenResponse response =
                authService.refreshAccessToken(request);

        assertThat(response.getAccessToken())
                .isEqualTo("new-access-token");

        assertThat(response.getRefreshToken())
                .isEqualTo("new-refresh-token");

        verify(refreshTokenService)
                .revokeToken("old-refresh-token");

        verify(refreshTokenService)
                .createRefreshToken(user);
    }
    @Test
    void shouldLogoutSuccessfully() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken(
                "refresh-token-to-revoke"
        );

        authService.logout(request);

        verify(refreshTokenService)
                .revokeToken(
                        "refresh-token-to-revoke"
                );
    }
//Add another test to ensure invalid tokens propagate correctly:
    @Test
    void shouldRejectLogoutWithInvalidRefreshToken() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("invalid-token");

        doThrow(
                new InvalidRefreshTokenException(
                        "Invalid refresh token"
                )
        ).when(refreshTokenService)
                .revokeToken("invalid-token");

        assertThatThrownBy(
                () -> authService.logout(request)
        )
                .isInstanceOf(
                        InvalidRefreshTokenException.class
                );

        verify(refreshTokenService)
                .revokeToken("invalid-token");
    }
}

