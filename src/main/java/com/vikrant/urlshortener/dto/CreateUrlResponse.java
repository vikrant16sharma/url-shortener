package com.vikrant.urlshortener.dto;

public class CreateUrlResponse {

    private String shortUrl;

    public CreateUrlResponse(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}