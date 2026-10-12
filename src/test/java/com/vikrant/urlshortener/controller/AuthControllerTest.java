package com.vikrant.urlshortener.controller;

import com.vikrant.urlshortener.dto.LoginRequest;
import com.vikrant.urlshortener.dto.LoginResponse;
import com.vikrant.urlshortener.Services.AuthService;
import com.vikrant.urlshortener.ratelimit.RateLimitFilter;
import com.vikrant.urlshortener.security.JwtAuthenticationFilter;

import com.vikrant.urlshortener.dto.*;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.vikrant.urlshortener.ratelimit.RateLimitService;
import com.vikrant.urlshortener.security.AuthenticatedUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private AuthenticatedUserService authenticatedUserService;

    @Test
    void shouldLoginSuccessfully() throws Exception {

        LoginResponse response =
                new LoginResponse(
                        1L,
                        "test@example.com",
                        "test-jwt-token",
                        "test-refresh-token"
                );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "password123"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("test@example.com")
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .value("test-jwt-token")
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .value("test-refresh-token")
                );
    }

    @Test
    void shouldRejectInvalidLoginRequest() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "email": "",
                                    "password": ""
                                }
                                """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully()
            throws Exception {

        RefreshTokenResponse response =
                new RefreshTokenResponse(
                        "new-access-token",
                        "new-refresh-token"
                );

        when(authService.refreshAccessToken(
                any(RefreshTokenRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken": "valid-refresh-token"
                    }
                    """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accessToken")
                                .value("new-access-token")
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .value("new-refresh-token")
                );;
    }
    @Test
    void shouldRejectBlankRefreshToken()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken": ""
                    }
                    """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLogoutSuccessfully()
            throws Exception {

        doNothing()
                .when(authService)
                .logout(any(RefreshTokenRequest.class));

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken": "valid-refresh-token"
                    }
                    """)
                )
                .andExpect(status().isNoContent());
    }
    @Test
    void shouldRejectLogoutWithBlankRefreshToken()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "refreshToken": ""
                    }
                    """)
                )
                .andExpect(status().isBadRequest());
    }
}