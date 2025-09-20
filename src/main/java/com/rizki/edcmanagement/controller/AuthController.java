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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> signUp(
            @Valid @RequestBody SignUpRequestDTO requestDTO) {
        AuthResponseDTO responseDTO = authService.signUp(requestDTO);
        SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                .message("User sign up successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> signIn(@Valid @RequestBody SignInRequestDTO requestDTO) {
        AuthResponseDTO responseDTO = authService.signIn(requestDTO);
        SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                .message("User sign in successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> refresh(
            @Valid @RequestBody RefreshRequestDTO requestDTO) {
        AuthResponseDTO responseDTO = authService.refresh(requestDTO);
        SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                .message("Token refreshed successfully")
                .data(responseDTO).build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<SuccessResponse<String>> signOut() {
        authService.signOut();
        SuccessResponse<String> response = SuccessResponse.<String>builder()
                .message("User signed out successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}