package com.vikrant.urlshortener.Services;

public interface JwtService {
    String generateToken(Long userId,String email);
    boolean isTokenValid(String token);

    String extractEmail(String token);

    Long extractUserId(String token);
}
