package com.rizki.edcmanagement.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.auth.request.RefreshRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignInRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.AuthResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.TokenResponse;
import com.rizki.edcmanagement.dto.auth.response.UserResponse;
import com.rizki.edcmanagement.exception.ResourceAlreadyExistsException;
import com.rizki.edcmanagement.exception.ResourceNotFoundException;
import com.rizki.edcmanagement.mapper.UserMapper;
import com.rizki.edcmanagement.model.User;
import com.rizki.edcmanagement.repository.UserRepository;
import com.rizki.edcmanagement.service.AuthService;
import com.rizki.edcmanagement.service.JwtService;
import com.rizki.edcmanagement.util.LoggingUtil;

@Service
public class AuthServiceImpl implements AuthService {
        @Autowired
        private UserRepository repository;

        @Autowired
        private UserMapper userMapper;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtService jwtService;

        @Override
        public User getCurrentAuthenticatedUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ResourceNotFoundException("User not authenticated");
                }

                String currentUsername = authentication.getName();

                // Find user by username (from access token)
                return repository.findByUsername(currentUsername)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        @Override
        @Transactional
        public AuthResponseDTO signUp(SignUpRequestDTO requestDTO) {
                LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNUP_STARTED",
                                "USERNAME", requestDTO.getUsername());

                try {
                        // Check if username already exists
                        LoggingUtil.logBusinessEvent("AUTH_USERNAME_CHECK_STARTED",
                                        "USERNAME", requestDTO.getUsername());

                        repository.findByUsername(requestDTO.getUsername())
                                        .ifPresent(user -> {
                                                LoggingUtil.logBusinessEvent("AUTH_USERNAME_ALREADY_EXISTS",
                                                                "USERNAME", requestDTO.getUsername());
                                                throw new ResourceAlreadyExistsException("Username is already exists");
                                        });

                        LoggingUtil.logBusinessEvent("AUTH_USERNAME_AVAILABLE",
                                        "USERNAME", requestDTO.getUsername());

                        // Create user entity
                        LoggingUtil.logBusinessEvent("AUTH_USER_ENTITY_CREATION_STARTED",
                                        "USERNAME", requestDTO.getUsername());

                        User user = userMapper.fromSignUpRequestToUser(requestDTO);
                        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

                        LoggingUtil.logBusinessEvent("AUTH_USER_PASSWORD_ENCODED",
                                        "USERNAME", requestDTO.getUsername());

                        // Save user to database
                        LoggingUtil.logBusinessEvent("AUTH_USER_DATABASE_SAVE_STARTED",
                                        "USERNAME", requestDTO.getUsername());

                        User savedUser = repository.save(user);

                        LoggingUtil.logBusinessEvent("AUTH_USER_DATABASE_SAVE_SUCCESS",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", savedUser.getUserId());

                        // Generate tokens
                        LoggingUtil.logBusinessEvent("AUTH_TOKEN_GENERATION_STARTED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", savedUser.getUserId());

                        String accessToken = jwtService.generateToken(savedUser.getUsername(), savedUser.getUserId());
                        String refreshToken = jwtService.generateRefreshToken(savedUser.getUsername(),
                                        savedUser.getUserId());

                        LoggingUtil.logBusinessEvent("AUTH_TOKENS_GENERATED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", savedUser.getUserId(),
                                        "ACCESS_TOKEN_LENGTH", accessToken.length(),
                                        "REFRESH_TOKEN_LENGTH", refreshToken.length());

                        // Update user with refresh token
                        savedUser.setRefreshToken(refreshToken);
                        User finalUser = repository.save(savedUser);

                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_SAVED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", savedUser.getUserId());

                        // Build response
                        UserResponse userResponse = userMapper.fromUserToUserResponse(finalUser);
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshToken)
                                        .build();

                        AuthResponseDTO response = AuthResponseDTO.builder()
                                        .user(userResponse)
                                        .tokens(tokenResponse)
                                        .build();

                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNUP_COMPLETED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", savedUser.getUserId(),
                                        "STATUS", "SUCCESS");

                        return response;

                } catch (ResourceAlreadyExistsException e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNUP_FAILED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNUP_ERROR",
                                        "USERNAME", requestDTO.getUsername(),
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                }
        }

        @Override
        @Transactional
        public AuthResponseDTO signIn(SignInRequestDTO requestDTO) {
                LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNIN_STARTED",
                                "USERNAME", requestDTO.getUsername());

                try {
                        // Find user by username
                        LoggingUtil.logBusinessEvent("AUTH_USER_LOOKUP_STARTED",
                                        "USERNAME", requestDTO.getUsername());

                        User user = repository.findByUsername(requestDTO.getUsername())
                                        .orElseThrow(() -> {
                                                LoggingUtil.logBusinessEvent("AUTH_USER_NOT_FOUND",
                                                                "USERNAME", requestDTO.getUsername());
                                                return new ResourceNotFoundException(
                                                                "Username or password is incorrect");
                                        });

                        LoggingUtil.logBusinessEvent("AUTH_USER_FOUND",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", user.getUserId());

                        // Verify password
                        LoggingUtil.logBusinessEvent("AUTH_PASSWORD_VERIFICATION_STARTED",
                                        "USERNAME", requestDTO.getUsername());

                        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
                                LoggingUtil.logBusinessEvent("AUTH_PASSWORD_VERIFICATION_FAILED",
                                                "USERNAME", requestDTO.getUsername());
                                throw new ResourceNotFoundException("Username or password is incorrect");
                        }

                        LoggingUtil.logBusinessEvent("AUTH_PASSWORD_VERIFICATION_SUCCESS",
                                        "USERNAME", requestDTO.getUsername());

                        // Generate tokens
                        LoggingUtil.logBusinessEvent("AUTH_TOKEN_GENERATION_STARTED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", user.getUserId());

                        String accessToken = jwtService.generateToken(user.getUsername(), user.getUserId());
                        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getUserId());

                        LoggingUtil.logBusinessEvent("AUTH_TOKENS_GENERATED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", user.getUserId(),
                                        "ACCESS_TOKEN_LENGTH", accessToken.length(),
                                        "REFRESH_TOKEN_LENGTH", refreshToken.length());

                        // Update user with new refresh token
                        user.setRefreshToken(refreshToken);
                        User modifiedUser = repository.save(user);

                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_UPDATED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", user.getUserId());

                        // Build response
                        UserResponse userResponse = userMapper.fromUserToUserResponse(modifiedUser);
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshToken)
                                        .build();

                        AuthResponseDTO response = AuthResponseDTO.builder()
                                        .user(userResponse)
                                        .tokens(tokenResponse)
                                        .build();

                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNIN_COMPLETED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "USER_ID", user.getUserId(),
                                        "STATUS", "SUCCESS");

                        return response;

                } catch (ResourceNotFoundException e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNIN_FAILED",
                                        "USERNAME", requestDTO.getUsername(),
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNIN_ERROR",
                                        "USERNAME", requestDTO.getUsername(),
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                }
        }

        @Override
        @Transactional
        public AuthResponseDTO refresh(RefreshRequestDTO requestDTO) {
                LoggingUtil.logBusinessEvent("AUTH_SERVICE_REFRESH_STARTED");

                try {
                        // Find user by refresh token
                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_LOOKUP_STARTED");

                        User user = repository.findByRefreshToken(requestDTO.getRefreshToken())
                                        .orElseThrow(() -> {
                                                LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_INVALID");
                                                return new ResourceNotFoundException("Invalid refresh token");
                                        });

                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_USER_FOUND",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId());

                        // Check token expiration
                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_EXPIRATION_CHECK_STARTED",
                                        "USERNAME", user.getUsername());

                        if (jwtService.isTokenExpired(requestDTO.getRefreshToken())) {
                                LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_EXPIRED",
                                                "USERNAME", user.getUsername());

                                user.setRefreshToken(null);
                                repository.save(user);

                                LoggingUtil.logBusinessEvent("AUTH_EXPIRED_REFRESH_TOKEN_CLEARED",
                                                "USERNAME", user.getUsername());

                                throw new ResourceNotFoundException("Refresh token has expired");
                        }

                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_VALID",
                                        "USERNAME", user.getUsername());

                        // Generate new access token
                        LoggingUtil.logBusinessEvent("AUTH_NEW_ACCESS_TOKEN_GENERATION_STARTED",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId());

                        String accessToken = jwtService.generateToken(user.getUsername(), user.getUserId());

                        LoggingUtil.logBusinessEvent("AUTH_NEW_ACCESS_TOKEN_GENERATED",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId(),
                                        "ACCESS_TOKEN_LENGTH", accessToken.length());

                        // Build response
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(requestDTO.getRefreshToken())
                                        .build();
                        UserResponse userResponse = userMapper.fromUserToUserResponse(user);

                        AuthResponseDTO response = AuthResponseDTO.builder()
                                        .user(userResponse)
                                        .tokens(tokenResponse)
                                        .build();

                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_REFRESH_COMPLETED",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId(),
                                        "STATUS", "SUCCESS");

                        return response;

                } catch (ResourceNotFoundException e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_REFRESH_FAILED",
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_REFRESH_ERROR",
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                }
        }

        @Override
        @Transactional
        public void signOut() {
                LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNOUT_STARTED");

                try {
                        // Get current authenticated user
                        LoggingUtil.logBusinessEvent("AUTH_CURRENT_USER_LOOKUP_STARTED");

                        User user = getCurrentAuthenticatedUser();

                        LoggingUtil.logBusinessEvent("AUTH_CURRENT_USER_FOUND",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId());

                        // Verify that user has a refresh token (meaning they are logged in)
                        if (user.getRefreshToken() == null || user.getRefreshToken().isEmpty()) {
                                LoggingUtil.logBusinessEvent("AUTH_USER_NOT_LOGGED_IN",
                                                "USERNAME", user.getUsername(),
                                                "USER_ID", user.getUserId());
                                throw new ResourceNotFoundException("User is not logged in");
                        }

                        LoggingUtil.logBusinessEvent("AUTH_REFRESH_TOKEN_CLEAR_STARTED",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId());

                        // Clear the refresh token from database
                        user.setRefreshToken(null);
                        repository.save(user);

                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNOUT_COMPLETED",
                                        "USERNAME", user.getUsername(),
                                        "USER_ID", user.getUserId(),
                                        "STATUS", "SUCCESS");

                } catch (ResourceNotFoundException e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNOUT_FAILED",
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        LoggingUtil.logBusinessEvent("AUTH_SERVICE_SIGNOUT_ERROR",
                                        "ERROR", e.getClass().getSimpleName(),
                                        "MESSAGE", e.getMessage());
                        throw e;
                }
        }
}