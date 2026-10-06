package com.vikrant.urlshortener.dto;

public class LoginResponse {

    private Long id;
    private String email;
    private String accessToken;

    public LoginResponse(Long id, String email,String accessToken) {
        this.id = id;
        this.email = email;
        this.accessToken = accessToken;
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
}