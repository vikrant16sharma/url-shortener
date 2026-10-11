package com.vikrant.urlshortener.Services.impl;

import com.vikrant.urlshortener.Services.RefreshTokenService;
import com.vikrant.urlshortener.config.JwtProperties;
import com.vikrant.urlshortener.entity.RefreshToken;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.exception.InvalidRefreshTokenException;
import com.vikrant.urlshortener.repository.RefreshTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String token =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(
                LocalDateTime.now()
                        .plusNanos(
                                jwtProperties
                                        .getRefreshExpirationMs()
                                        * 1_000_000
                        )
        );
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(
                LocalDateTime.now()
        );

        return refreshTokenRepository.save(
                refreshToken
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(
            String token
    ) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new InvalidRefreshTokenException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"
                                )
                        );

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}