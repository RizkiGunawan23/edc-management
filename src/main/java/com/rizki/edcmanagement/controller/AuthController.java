package com.rizki.edcmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
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
                .message("User registered successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}