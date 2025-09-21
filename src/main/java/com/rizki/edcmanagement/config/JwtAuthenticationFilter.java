package com.rizki.edcmanagement.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rizki.edcmanagement.service.JwtService;
import com.rizki.edcmanagement.util.LoggingUtil;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String requestUri = request.getRequestURI();
        String clientIp = getClientIpAddress(request);

        LoggingUtil.setCorrelationContext(correlationId, "JWT_FILTER", "JWT_AUTHENTICATION");

        try {
            LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_STARTED",
                    "REQUEST_URI", requestUri,
                    "CLIENT_IP", clientIp,
                    "CORRELATION_ID", correlationId);

            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_NO_AUTH_HEADER",
                        "REQUEST_URI", requestUri,
                        "CLIENT_IP", clientIp);
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);

            // Validate JWT token format
            if (jwt == null || jwt.trim().isEmpty() || "null".equals(jwt)) {
                LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_INVALID_TOKEN_FORMAT",
                        "REQUEST_URI", requestUri,
                        "CLIENT_IP", clientIp,
                        "REASON", "Token is null or empty");

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Basic JWT format validation (should contain exactly 2 dots)
            if (!jwt.contains(".") || jwt.split("\\.").length != 3) {
                LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_INVALID_TOKEN_STRUCTURE",
                        "REQUEST_URI", requestUri,
                        "CLIENT_IP", clientIp,
                        "REASON", "Invalid JWT structure");

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_TOKEN_EXTRACTION_SUCCESS",
                    "REQUEST_URI", requestUri,
                    "CLIENT_IP", clientIp,
                    "TOKEN_LENGTH", jwt.length());

            try {
                userEmail = jwtService.extractUsername(jwt);

                LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_USERNAME_EXTRACTED",
                        "REQUEST_URI", requestUri,
                        "CLIENT_IP", clientIp,
                        "USERNAME", userEmail);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_USER_DETAILS_LOOKUP_STARTED",
                            "USERNAME", userEmail,
                            "REQUEST_URI", requestUri);

                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                    LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_USER_DETAILS_LOADED",
                            "USERNAME", userEmail,
                            "AUTHORITIES", userDetails.getAuthorities().size());

                    boolean isTokenValid = jwtService.isTokenValid(jwt);

                    if (isTokenValid) {
                        LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_AUTHENTICATION_SUCCESS",
                                "USERNAME", userEmail,
                                "REQUEST_URI", requestUri,
                                "CLIENT_IP", clientIp);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_TOKEN_INVALID",
                                "USERNAME", userEmail,
                                "REQUEST_URI", requestUri,
                                "CLIENT_IP", clientIp);

                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            } catch (BadCredentialsException e) {
                LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_BAD_CREDENTIALS",
                        "REQUEST_URI", requestUri,
                        "CLIENT_IP", clientIp,
                        "ERROR", e.getMessage());

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_PROCESSING_ERROR",
                        "REQUEST_URI", requestUri,
                        "CLIENT_IP", clientIp,
                        "ERROR", e.getClass().getSimpleName(),
                        "MESSAGE", e.getMessage());

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            LoggingUtil.logBusinessEvent(logger, "JWT_FILTER_COMPLETED",
                    "REQUEST_URI", requestUri,
                    "CLIENT_IP", clientIp,
                    "AUTHENTICATED", SecurityContextHolder.getContext().getAuthentication() != null);

            filterChain.doFilter(request, response);
        } finally {
            LoggingUtil.clearCorrelationContext();
        }
    }

    // Helper method untuk mendapatkan client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}