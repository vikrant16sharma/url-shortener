package com.vikrant.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateUrlRequest {
    @NotBlank(message = "URL cannot be Blank")
    @Pattern(
            regexp = "https?://.+",
            message = "URL must start with http:// or https://"
    )
    private String url;
    public CreateUrlRequest(String url){
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
}