package com.rizki.edcmanagement.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDTO {
    private TokenResponse tokens;
    private UserResponse user;
}