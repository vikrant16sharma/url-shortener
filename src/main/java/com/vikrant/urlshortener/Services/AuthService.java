package com.vikrant.urlshortener.Services;

import com.vikrant.urlshortener.dto.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshAccessToken(
            RefreshTokenRequest request
    );
    void logout(RefreshTokenRequest request);
}