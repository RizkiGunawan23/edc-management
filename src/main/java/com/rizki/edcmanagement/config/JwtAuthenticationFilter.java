package com.rizki.edcmanagement.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = LoggingUtil.generateCorrelationId();
        String requestUri = request.getRequestURI();
        String clientIp = LoggingUtil.getClientIpAddress(request);

        LoggingUtil.setMDC(correlationId, clientIp, "JwtAuthenticationFilter");

        try {
            LoggingUtil.logBusinessEvent("JWT_FILTER_STARTED",
                    "JWT filter started for request: " + requestUri +
                            ", clientIp: " + clientIp);

            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                LoggingUtil.logBusinessEvent("JWT_FILTER_NO_AUTH_HEADER",
                        "No authorization header found for request: " + requestUri +
                                ", clientIp: " + clientIp);
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);

            // Validate JWT token format
            if (jwt == null || jwt.trim().isEmpty() || "null".equals(jwt)) {
                LoggingUtil.logBusinessEvent("JWT_FILTER_INVALID_TOKEN_FORMAT",
                        "Invalid token format for request: " + requestUri +
                                ", clientIp: " + clientIp +
                                ", reason: Token is null or empty");

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Basic JWT format validation (should contain exactly 2 dots)
            if (!jwt.contains(".") || jwt.split("\\.").length != 3) {
                LoggingUtil.logBusinessEvent("JWT_FILTER_INVALID_TOKEN_STRUCTURE",
                        "Invalid token structure for request: " + requestUri +
                                ", clientIp: " + clientIp +
                                ", reason: Invalid JWT structure");

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            LoggingUtil.logBusinessEvent("JWT_FILTER_TOKEN_EXTRACTION_SUCCESS",
                    "Token extraction successful for request: " + requestUri +
                            ", clientIp: " + clientIp +
                            ", tokenLength: " + jwt.length());

            try {
                userEmail = jwtService.extractUsername(jwt);

                LoggingUtil.logBusinessEvent("JWT_FILTER_USERNAME_EXTRACTED",
                        "Username extracted for request: " + requestUri +
                                ", clientIp: " + clientIp +
                                ", username: " + userEmail);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    LoggingUtil.logBusinessEvent("JWT_FILTER_USER_DETAILS_LOOKUP_STARTED",
                            "User details lookup started for username: " + userEmail +
                                    ", request: " + requestUri);

                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                    LoggingUtil.logBusinessEvent("JWT_FILTER_USER_DETAILS_LOADED",
                            "User details loaded for username: " + userEmail +
                                    ", authorities: " + userDetails.getAuthorities().size());

                    boolean isTokenValid = jwtService.isTokenValid(jwt);

                    if (isTokenValid) {
                        LoggingUtil.logBusinessEvent("JWT_FILTER_AUTHENTICATION_SUCCESS",
                                "Authentication successful for username: " + userEmail +
                                        ", request: " + requestUri +
                                        ", clientIp: " + clientIp);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        LoggingUtil.logBusinessEvent("JWT_FILTER_TOKEN_INVALID",
                                "Token invalid for username: " + userEmail +
                                        ", request: " + requestUri +
                                        ", clientIp: " + clientIp);

                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            } catch (BadCredentialsException e) {
                LoggingUtil.logBusinessEvent("JWT_FILTER_BAD_CREDENTIALS",
                        "Bad credentials for request: " + requestUri +
                                ", clientIp: " + clientIp +
                                ", error: " + e.getMessage());

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                LoggingUtil.logBusinessEvent("JWT_FILTER_PROCESSING_ERROR",
                        "Processing error for request: " + requestUri +
                                ", clientIp: " + clientIp +
                                ", error: " + e.getClass().getSimpleName() +
                                ", message: " + e.getMessage());

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            LoggingUtil.logBusinessEvent("JWT_FILTER_COMPLETED",
                    "JWT filter completed for request: " + requestUri +
                            ", clientIp: " + clientIp +
                            ", authenticated: " + (SecurityContextHolder.getContext().getAuthentication() != null));

            filterChain.doFilter(request, response);
        } finally {
            LoggingUtil.clearMDC();
        }
    }
}