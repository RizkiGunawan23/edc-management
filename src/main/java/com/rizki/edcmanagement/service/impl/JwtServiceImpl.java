package com.rizki.edcmanagement.service.impl;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rizki.edcmanagement.service.JwtService;
import com.rizki.edcmanagement.util.LoggingUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private int JWT_EXPIRATION;

    @Value("${application.security.jwt.refresh-expiration}")
    private int REFRESH_EXPIRATION;

    private Key signInKey;

    public Key getSignInKey() {
        if (signInKey == null) {
            signInKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
        return signInKey;
    }

    public String generateToken(String username, Long userId) {
        LoggingUtil.logBusinessEvent("JWT_ACCESS_TOKEN_GENERATION_STARTED",
                "username", username,
                "userId", userId.toString());

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            String token = createToken(claims, username, JWT_EXPIRATION);

            LoggingUtil.logBusinessEvent("JWT_ACCESS_TOKEN_GENERATION_SUCCESS",
                    "USERNAME", username,
                    "USER_ID", userId,
                    "TOKEN_LENGTH", token.length(),
                    "EXPIRATION_MS", JWT_EXPIRATION);

            return token;
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("JWT_ACCESS_TOKEN_GENERATION_ERROR",
                    "USERNAME", username,
                    "USER_ID", userId,
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());
            throw e;
        }
    }

    public String generateRefreshToken(String username, Long userId) {
        LoggingUtil.logBusinessEvent("JWT_REFRESH_TOKEN_GENERATION_STARTED",
                "USERNAME", username,
                "USER_ID", userId);

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            String token = createToken(claims, username, REFRESH_EXPIRATION);

            LoggingUtil.logBusinessEvent("JWT_REFRESH_TOKEN_GENERATION_SUCCESS",
                    "USERNAME", username,
                    "USER_ID", userId,
                    "TOKEN_LENGTH", token.length(),
                    "EXPIRATION_MS", REFRESH_EXPIRATION);

            return token;
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("JWT_REFRESH_TOKEN_GENERATION_ERROR",
                    "USERNAME", username,
                    "USER_ID", userId,
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());
            throw e;
        }
    }

    public String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        LoggingUtil.logBusinessEvent("JWT_TOKEN_VALIDATION_STARTED",
                "TOKEN_LENGTH", token.length());

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);

            LoggingUtil.logBusinessEvent("JWT_TOKEN_VALIDATION_SUCCESS",
                    "TOKEN_LENGTH", token.length());

            return true;
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("JWT_TOKEN_VALIDATION_FAILED",
                    "TOKEN_LENGTH", token.length(),
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());

            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        LoggingUtil.logBusinessEvent("JWT_TOKEN_EXPIRATION_CHECK_STARTED",
                "TOKEN_LENGTH", token.length());

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            boolean isExpired = claims.getExpiration().before(new Date());

            LoggingUtil.logBusinessEvent("JWT_TOKEN_EXPIRATION_CHECK_COMPLETED",
                    "TOKEN_LENGTH", token.length(),
                    "IS_EXPIRED", isExpired,
                    "EXPIRATION_DATE", claims.getExpiration());

            return isExpired;
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("JWT_TOKEN_EXPIRATION_CHECK_ERROR",
                    "TOKEN_LENGTH", token.length(),
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());

            return true; // If token is invalid, consider it expired
        }
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.get("userId").toString());
    }
}