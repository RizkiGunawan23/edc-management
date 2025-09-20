package com.rizki.edcmanagement.service;

import java.security.Key;
import java.util.Map;

public interface JwtService {
    Key getSignInKey();

    String generateToken(String username, Long userId);

    String generateRefreshToken(String username, Long userId);

    String createToken(Map<String, Object> claims, String subject, long expiration);
}