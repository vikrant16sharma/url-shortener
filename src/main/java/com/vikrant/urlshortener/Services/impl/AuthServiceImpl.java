package com.vikrant.urlshortener.Services.impl;
import com.vikrant.urlshortener.Services.JwtService;
import com.vikrant.urlshortener.dto.RegisterRequest;
import com.vikrant.urlshortener.dto.RegisterResponse;
import com.vikrant.urlshortener.entity.User;
import com.vikrant.urlshortener.exception.EmailAlreadyRegisteredException;
import com.vikrant.urlshortener.repository.UserRepository;
import com.vikrant.urlshortener.Services.AuthService;
import com.vikrant.urlshortener.dto.LoginResponse;
import com.vikrant.urlshortener.dto.LoginRequest;
import com.vikrant.urlshortener.exception.InvalidCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    @Transactional(readOnly = true)
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

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                accessToken
        );
    }
}