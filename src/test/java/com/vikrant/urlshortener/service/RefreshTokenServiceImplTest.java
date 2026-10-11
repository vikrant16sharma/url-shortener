package com.vikrant.urlshortener.service;

import com.vikrant.urlshortener.Services.impl.RefreshTokenServiceImpl;
import com.vikrant.urlshortener.config.JwtProperties;
import com.vikrant.urlshortener.entity.RefreshToken;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.exception.InvalidRefreshTokenException;
import com.vikrant.urlshortener.repository.RefreshTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");
    }

    // ---------------------------------------------------------
    // CREATE REFRESH TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldCreateRefreshToken() {

        when(jwtProperties.getRefreshExpirationMs())
                .thenReturn(604800000L);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        RefreshToken result =
                refreshTokenService.createRefreshToken(user);

        assertNotNull(result);

        assertNotNull(result.getToken());

        assertEquals(user, result.getUser());

        assertFalse(result.isRevoked());

        assertNotNull(result.getExpiresAt());

        assertNotNull(result.getCreatedAt());

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    // ---------------------------------------------------------
    // VERIFY VALID TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldVerifyValidRefreshToken() {

        String token = "valid-refresh-token";

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setId(1L);
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(refreshToken));

        RefreshToken result =
                refreshTokenService.verifyRefreshToken(token);

        assertNotNull(result);

        assertEquals(token, result.getToken());

        assertEquals(user, result.getUser());

        assertFalse(result.isRevoked());

        verify(refreshTokenRepository)
                .findByToken(token);
    }

    // ---------------------------------------------------------
    // UNKNOWN TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldRejectUnknownRefreshToken() {

        String token = "unknown-token";

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService
                        .verifyRefreshToken(token)
        );

        verify(refreshTokenRepository)
                .findByToken(token);
    }

    // ---------------------------------------------------------
    // REVOKED TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldRejectRevokedRefreshToken() {

        String token = "revoked-token";

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setRevoked(true);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(refreshToken));

        InvalidRefreshTokenException exception =
                assertThrows(
                        InvalidRefreshTokenException.class,
                        () -> refreshTokenService
                                .verifyRefreshToken(token)
                );

        assertEquals(
                "Refresh token has been revoked",
                exception.getMessage()
        );

        verify(refreshTokenRepository)
                .findByToken(token);
    }

    // ---------------------------------------------------------
    // EXPIRED TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldRejectExpiredRefreshToken() {

        String token = "expired-token";

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(refreshToken));

        InvalidRefreshTokenException exception =
                assertThrows(
                        InvalidRefreshTokenException.class,
                        () -> refreshTokenService
                                .verifyRefreshToken(token)
                );

        assertEquals(
                "Refresh token has expired",
                exception.getMessage()
        );

        verify(refreshTokenRepository)
                .findByToken(token);
    }

    // ---------------------------------------------------------
    // REVOKE TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldRevokeRefreshToken() {

        String token = "token-to-revoke";

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeToken(token);

        assertTrue(refreshToken.isRevoked());

        verify(refreshTokenRepository)
                .findByToken(token);

        verify(refreshTokenRepository)
                .save(refreshToken);
    }

    // ---------------------------------------------------------
    // REVOKE UNKNOWN TOKEN
    // ---------------------------------------------------------

    @Test
    void shouldRejectRevokingUnknownToken() {

        String token = "unknown-token";

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.revokeToken(token)
        );

        verify(refreshTokenRepository)
                .findByToken(token);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }
}