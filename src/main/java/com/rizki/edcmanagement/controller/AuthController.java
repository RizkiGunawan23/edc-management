package com.rizki.edcmanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizki.edcmanagement.dto.auth.request.RefreshRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignInRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.AuthResponseDTO;
import com.rizki.edcmanagement.dto.common.SuccessResponse;
import com.rizki.edcmanagement.service.AuthService;
import com.rizki.edcmanagement.util.LoggingUtil;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> signUp(
            @Valid @RequestBody SignUpRequestDTO requestDTO, HttpServletRequest request) {
        // Set correlation context untuk tracking
        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String clientIp = getClientIpAddress(request);
        LoggingUtil.setCorrelationContext(correlationId, requestDTO.getUsername(), "AUTH_SIGNUP");

        try {
            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNUP_REQUEST_RECEIVED",
                    "USERNAME", requestDTO.getUsername(),
                    "CLIENT_IP", clientIp,
                    "CORRELATION_ID", correlationId);

            long startTime = System.currentTimeMillis();
            AuthResponseDTO responseDTO = authService.signUp(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNUP_SUCCESS",
                    "USERNAME", requestDTO.getUsername(),
                    "USER_ID", responseDTO.getUser().getId(),
                    "PROCESSING_TIME_MS", processingTime,
                    "CLIENT_IP", clientIp);

            LoggingUtil.logPerformance("AUTH_SIGNUP", processingTime);

            SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                    .message("User sign up successfully")
                    .data(responseDTO)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNUP_FAILED",
                    "USERNAME", requestDTO.getUsername(),
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage(),
                    "CLIENT_IP", clientIp);
            throw e;
        } finally {
            LoggingUtil.clearCorrelationContext();
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> signIn(
            @Valid @RequestBody SignInRequestDTO requestDTO, HttpServletRequest request) {
        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String clientIp = getClientIpAddress(request);
        LoggingUtil.setCorrelationContext(correlationId, requestDTO.getUsername(), "AUTH_SIGNIN");

        try {
            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNIN_REQUEST_RECEIVED",
                    "USERNAME", requestDTO.getUsername(),
                    "CLIENT_IP", clientIp,
                    "CORRELATION_ID", correlationId);

            long startTime = System.currentTimeMillis();
            AuthResponseDTO responseDTO = authService.signIn(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNIN_SUCCESS",
                    "USERNAME", requestDTO.getUsername(),
                    "USER_ID", responseDTO.getUser().getId(),
                    "PROCESSING_TIME_MS", processingTime,
                    "CLIENT_IP", clientIp);

            LoggingUtil.logPerformance("AUTH_SIGNIN", processingTime);

            SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                    .message("User sign in successfully")
                    .data(responseDTO)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNIN_FAILED",
                    "USERNAME", requestDTO.getUsername(),
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage(),
                    "CLIENT_IP", clientIp);
            throw e;
        } finally {
            LoggingUtil.clearCorrelationContext();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> refresh(
            @Valid @RequestBody RefreshRequestDTO requestDTO, HttpServletRequest request) {
        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String clientIp = getClientIpAddress(request);
        LoggingUtil.setCorrelationContext(correlationId, "REFRESH_TOKEN_USER", "AUTH_REFRESH");

        try {
            LoggingUtil.logBusinessEvent(logger, "AUTH_REFRESH_REQUEST_RECEIVED",
                    "CLIENT_IP", clientIp,
                    "CORRELATION_ID", correlationId);

            long startTime = System.currentTimeMillis();
            AuthResponseDTO responseDTO = authService.refresh(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent(logger, "AUTH_REFRESH_SUCCESS",
                    "USER_ID", responseDTO.getUser().getId(),
                    "USERNAME", responseDTO.getUser().getUsername(),
                    "PROCESSING_TIME_MS", processingTime,
                    "CLIENT_IP", clientIp);

            LoggingUtil.logPerformance("AUTH_REFRESH", processingTime);

            SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                    .message("Token refreshed successfully")
                    .data(responseDTO).build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent(logger, "AUTH_REFRESH_FAILED",
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage(),
                    "CLIENT_IP", clientIp);
            throw e;
        } finally {
            LoggingUtil.clearCorrelationContext();
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity<SuccessResponse<String>> signOut(HttpServletRequest request) {
        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String clientIp = getClientIpAddress(request);
        LoggingUtil.setCorrelationContext(correlationId, "AUTHENTICATED_USER", "AUTH_SIGNOUT");

        try {
            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNOUT_REQUEST_RECEIVED",
                    "CLIENT_IP", clientIp,
                    "CORRELATION_ID", correlationId);

            long startTime = System.currentTimeMillis();
            authService.signOut();
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNOUT_SUCCESS",
                    "PROCESSING_TIME_MS", processingTime,
                    "CLIENT_IP", clientIp);

            LoggingUtil.logPerformance("AUTH_SIGNOUT", processingTime);

            SuccessResponse<String> response = SuccessResponse.<String>builder()
                    .message("User signed out successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent(logger, "AUTH_SIGNOUT_FAILED",
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage(),
                    "CLIENT_IP", clientIp);
            throw e;
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