package com.rizki.edcmanagement.service;

import java.security.Key;
import java.util.Map;

public interface JwtService {
    Key getSignInKey();

    String generateToken(String username, Long userId);

    String generateRefreshToken(String username, Long userId);

    String createToken(Map<String, Object> claims, String subject, long expiration);

    boolean isTokenValid(String token);

    boolean isTokenExpired(String token);

    String extractUsername(String token);

    Long extractUserId(String token);
}