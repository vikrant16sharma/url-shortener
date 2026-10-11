package com.vikrant.urlshortener.Services;

import com.vikrant.urlshortener.entity.RefreshToken;
import com.vikrant.urlshortener.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeToken(String token);
}