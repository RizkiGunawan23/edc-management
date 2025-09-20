package com.rizki.edcmanagement.dto.auth.request;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequestDTO {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}