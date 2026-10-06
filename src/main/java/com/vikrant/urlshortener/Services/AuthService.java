package com.vikrant.urlshortener.Services;

import com.vikrant.urlshortener.dto.RegisterRequest;
import com.vikrant.urlshortener.dto.RegisterResponse;
import com.vikrant.urlshortener.dto.LoginRequest;
import com.vikrant.urlshortener.dto.LoginResponse;
public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}