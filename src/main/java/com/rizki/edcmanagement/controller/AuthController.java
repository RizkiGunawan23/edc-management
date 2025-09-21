package com.rizki.edcmanagement.controller;

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
    @Autowired
    private AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> signUp(
            @Valid @RequestBody SignUpRequestDTO requestDTO, HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        LoggingUtil.setMDC(correlationId, clientIp, "AuthController");

        try {
            LoggingUtil.logBusinessEvent("AUTH_SIGNUP_REQUEST_RECEIVED",
                    "Sign up request received for username: " + requestDTO.getUsername() +
                            ", clientIp: " + clientIp);

            long startTime = System.currentTimeMillis();
            AuthResponseDTO responseDTO = authService.signUp(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent("AUTH_SIGNUP_SUCCESS",
                    "Sign up successful for username: " + requestDTO.getUsername() +
                            ", userId: " + responseDTO.getUser().getId() +
                            ", processingTime: " + processingTime + "ms" +
                            ", clientIp: " + clientIp);

            LoggingUtil.logPerformance("AUTH_SIGNUP", processingTime);

            SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                    .message("User sign up successfully")
                    .data(responseDTO)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("AUTH_SIGNUP_FAILED",
                    "Sign up failed for username: " + requestDTO.getUsername() +
                            ", error: " + e.getClass().getSimpleName() +
                            ", message: " + e.getMessage() +
                            ", clientIp: " + clientIp);
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> signIn(
            @Valid @RequestBody SignInRequestDTO requestDTO, HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        LoggingUtil.setMDC(correlationId, clientIp, "AuthController");

        try {
            LoggingUtil.logBusinessEvent("AUTH_SIGNIN_REQUEST_RECEIVED",
                    "Sign in request received for username: " + requestDTO.getUsername() +
                            ", clientIp: " + clientIp);

            long startTime = System.currentTimeMillis();
            AuthResponseDTO responseDTO = authService.signIn(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent("AUTH_SIGNIN_SUCCESS",
                    "Sign in successful for username: " + requestDTO.getUsername() +
                            ", userId: " + responseDTO.getUser().getId() +
                            ", processingTime: " + processingTime + "ms" +
                            ", clientIp: " + clientIp);

            LoggingUtil.logPerformance("AUTH_SIGNIN", processingTime);

            SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                    .message("User sign in successfully")
                    .data(responseDTO)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("AUTH_SIGNIN_FAILED",
                    "Sign in failed for username: " + requestDTO.getUsername() +
                            ", error: " + e.getClass().getSimpleName() +
                            ", message: " + e.getMessage() +
                            ", clientIp: " + clientIp);
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> refresh(
            @Valid @RequestBody RefreshRequestDTO requestDTO, HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        LoggingUtil.setMDC(correlationId, clientIp, "AuthController");

        try {
            LoggingUtil.logBusinessEvent("AUTH_REFRESH_REQUEST_RECEIVED",
                    "Token refresh request received, clientIp: " + clientIp);

            long startTime = System.currentTimeMillis();
            AuthResponseDTO responseDTO = authService.refresh(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent("AUTH_REFRESH_SUCCESS",
                    "Token refresh successful for userId: " + responseDTO.getUser().getId() +
                            ", username: " + responseDTO.getUser().getUsername() +
                            ", processingTime: " + processingTime + "ms" +
                            ", clientIp: " + clientIp);

            LoggingUtil.logPerformance("AUTH_REFRESH", processingTime);

            SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                    .message("Token refreshed successfully")
                    .data(responseDTO).build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("AUTH_REFRESH_FAILED",
                    "Token refresh failed, error: " + e.getClass().getSimpleName() +
                            ", message: " + e.getMessage() +
                            ", clientIp: " + clientIp);
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity<SuccessResponse<String>> signOut(HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        LoggingUtil.setMDC(correlationId, clientIp, "AuthController");

        try {
            LoggingUtil.logBusinessEvent("AUTH_SIGNOUT_REQUEST_RECEIVED",
                    "Sign out request received, clientIp: " + clientIp);

            long startTime = System.currentTimeMillis();
            authService.signOut();
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent("AUTH_SIGNOUT_SUCCESS",
                    "Sign out successful, processingTime: " + processingTime + "ms" +
                            ", clientIp: " + clientIp);

            LoggingUtil.logPerformance("AUTH_SIGNOUT", processingTime);

            SuccessResponse<String> response = SuccessResponse.<String>builder()
                    .message("User signed out successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("AUTH_SIGNOUT_FAILED",
                    "Sign out failed, error: " + e.getClass().getSimpleName() +
                            ", message: " + e.getMessage() +
                            ", clientIp: " + clientIp);
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }
}