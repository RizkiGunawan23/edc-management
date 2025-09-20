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

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // Validate JWT token format
        if (jwt == null || jwt.trim().isEmpty() || "null".equals(jwt)) {
            // Invalid token format - clear context and continue to let Spring Security
            // handle authentication failure
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // Basic JWT format validation (should contain exactly 2 dots)
        if (!jwt.contains(".") || jwt.split("\\.").length != 3) {
            // Invalid token format - clear context and continue to let Spring Security
            // handle authentication failure
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                boolean isTokenValid = jwtService.isTokenValid(jwt);

                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Token is not valid - clear context and continue to let Spring Security handle
                    // authentication failure
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        } catch (BadCredentialsException e) {
            // Clear context and continue to let Spring Security handle authentication
            // failure
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            // For other exceptions (like JWT parsing errors, user not found, etc.)
            // Clear context and continue to let Spring Security handle authentication
            // failure
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}