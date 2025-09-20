package com.rizki.edcmanagement.dto.echo.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EchoRequestDTO {
    @NotBlank(message = "Terminal ID is required")
    @Size(min = 9, max = 15, message = "Terminal ID must be between 9 and 15 characters")
    @Pattern(regexp = "^(EDC|ATM|POS|KIOSK)-[A-Z]{3}-[0-9]{3}$", message = "Terminal ID must follow pattern: {TYPE}-{LOCATION}-{SEQUENCE}. "
            +
            "Valid types: EDC, ATM, POS, KIOSK. Location must be 3 uppercase letters. " +
            "Sequence must be 3 digits (001-999). Examples: EDC-JKT-001, ATM-BDG-045, POS-SBY-123, KIOSK-DPS-001")
    private String terminalId;
}