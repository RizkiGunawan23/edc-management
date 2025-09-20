package com.rizki.edcmanagement.service;

import com.rizki.edcmanagement.dto.auth.request.RefreshRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignInRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.AuthResponseDTO;
import com.rizki.edcmanagement.model.User;

public interface AuthService {
    AuthResponseDTO signUp(SignUpRequestDTO requestDTO);

    AuthResponseDTO signIn(SignInRequestDTO requestDTO);

    AuthResponseDTO refresh(RefreshRequestDTO requestDTO);

    void signOut();

    User getCurrentAuthenticatedUser();
}