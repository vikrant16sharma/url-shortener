package com.vikrant.urlshortener.dto;

public class LoginResponse {

    private Long id;
    private String email;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(
            Long id,
            String email,
            String accessToken,
            String refreshToken
    ) {
        this.id = id;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}