package com.rizki.edcmanagement.service;

import com.rizki.edcmanagement.dto.auth.request.RefreshRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignInRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.RefreshResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.SignInResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.SignUpResponseDTO;

public interface AuthService {
    SignUpResponseDTO signUp(SignUpRequestDTO requestDTO);

    SignInResponseDTO signIn(SignInRequestDTO requestDTO);

    RefreshResponseDTO refresh(RefreshRequestDTO requestDTO);
}