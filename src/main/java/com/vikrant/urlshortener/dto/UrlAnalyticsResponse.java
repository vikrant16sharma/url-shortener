package com.vikrant.urlshortener.dto;

import java.time.LocalDateTime;

public class UrlAnalyticsResponse {

    private String shortCode;
    private String originalUrl;
    private long totalClicks;
    private LocalDateTime firstClickedAt;
    private LocalDateTime lastClickedAt;

    public UrlAnalyticsResponse(
            String shortCode,
            String originalUrl,
            long totalClicks,
            LocalDateTime firstClickedAt,
            LocalDateTime lastClickedAt) {

        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.totalClicks = totalClicks;
        this.firstClickedAt = firstClickedAt;
        this.lastClickedAt = lastClickedAt;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public LocalDateTime getFirstClickedAt() {
        return firstClickedAt;
    }

    public LocalDateTime getLastClickedAt() {
        return lastClickedAt;
    }
}