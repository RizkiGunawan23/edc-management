package com.rizki.edcmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizki.edcmanagement.dto.auth.request.RefreshRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignInRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.RefreshResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.SignInResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.SignUpResponseDTO;
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
    public ResponseEntity<SuccessResponse<SignUpResponseDTO>> signUp(
            @Valid @RequestBody SignUpRequestDTO requestDTO) {
        SignUpResponseDTO responseDTO = authService.signUp(requestDTO);
        SuccessResponse<SignUpResponseDTO> response = SuccessResponse.<SignUpResponseDTO>builder()
                .message("User sign up successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SuccessResponse<SignInResponseDTO>> signIn(@Valid @RequestBody SignInRequestDTO requestDTO) {
        SignInResponseDTO responseDTO = authService.signIn(requestDTO);
        SuccessResponse<SignInResponseDTO> response = SuccessResponse.<SignInResponseDTO>builder()
                .message("User sign in successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshResponseDTO>> refresh(
            @Valid @RequestBody RefreshRequestDTO requestDTO) {
        RefreshResponseDTO responseDTO = authService.refresh(requestDTO);
        SuccessResponse<RefreshResponseDTO> response = SuccessResponse.<RefreshResponseDTO>builder()
                .message("Token refreshed successfully")
                .data(responseDTO).build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}