package com.vikrant.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class CreateUrlRequest {
    @NotBlank(message = "URL cannot be Blank")
    @Pattern(
            regexp = "https?://.+",
            message = "URL must start with http:// or https://"
    )
    private String url;
    private LocalDateTime expiresAt;
    public CreateUrlRequest(String url){
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    public LocalDateTime getExpiresAt(){
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt){
        this.expiresAt = expiresAt;
    }
}