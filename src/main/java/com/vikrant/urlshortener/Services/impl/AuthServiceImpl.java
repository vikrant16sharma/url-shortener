package com.vikrant.urlshortener.Services.impl;
import com.vikrant.urlshortener.Services.JwtService;
import com.vikrant.urlshortener.Services.RefreshTokenService;
import com.vikrant.urlshortener.dto.RegisterRequest;
import com.vikrant.urlshortener.dto.RegisterResponse;
import com.vikrant.urlshortener.entity.RefreshToken;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.exception.EmailAlreadyRegisteredException;
import com.vikrant.urlshortener.repository.UserRepository;
import com.vikrant.urlshortener.Services.AuthService;
import com.vikrant.urlshortener.dto.LoginResponse;
import com.vikrant.urlshortener.dto.LoginRequest;
import com.vikrant.urlshortener.dto.*;
import com.vikrant.urlshortener.exception.InvalidCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(
                    "Email is already registered"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.getPassword());

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHash);

        User savedUser =
                userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail()
        );
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }
        String accessToken =
                jwtService.generateToken(
                        user.getId(),
                        user.getEmail()
                );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken.getToken()
        );
    }
    @Override
    @Transactional
    public RefreshTokenResponse refreshAccessToken(
            RefreshTokenRequest request
    ) {

        RefreshToken oldRefreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        User user = oldRefreshToken.getUser();

        // Revoke the old refresh token
        refreshTokenService.revokeToken(
                oldRefreshToken.getToken()
        );

        // Generate a new refresh token
        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        // Generate a new access token
        String accessToken =
                jwtService.generateToken(
                        user.getId(),
                        user.getEmail()
                );

        return new RefreshTokenResponse(
                accessToken,
                newRefreshToken.getToken()
        );
    }
    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {

        refreshTokenService.revokeToken(
                request.getRefreshToken()
        );
    }
}