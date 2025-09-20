package com.rizki.edcmanagement.dto.auth.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpResponseDTO {
    private TokenResponse tokens;
    private UserResponse user;
}