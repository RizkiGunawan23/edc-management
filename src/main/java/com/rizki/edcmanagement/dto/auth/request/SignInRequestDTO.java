package com.rizki.edcmanagement.dto.auth.request;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class SignInRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}