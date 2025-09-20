package com.rizki.edcmanagement.dto.terminal.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.rizki.edcmanagement.validation.ValidTerminalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTerminalEDCRequestDTO {
    @NotBlank(message = "Terminal ID is required")
    @Size(min = 9, max = 15, message = "Terminal ID must be between 9 and 15 characters")
    @Pattern(regexp = "^(EDC|ATM|POS|KIOSK)-[A-Z]{3}-[0-9]{3}$", message = "Terminal ID must follow pattern: {TYPE}-{LOCATION}-{SEQUENCE}. "
            +
            "Valid types: EDC, ATM, POS, KIOSK. Location must be 3 uppercase letters. " +
            "Sequence must be 3 digits (001-999). Examples: EDC-JKT-001, ATM-BDG-045, POS-SBY-123, KIOSK-DPS-001")
    private String terminalId;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    /**
     * OPTIONAL: Initial status of the terminal
     * If not provided, defaults to INACTIVE for new terminals
     * Accepts values: ACTIVE, INACTIVE, MAINTENANCE, OUT_OF_SERVICE
     */
    @ValidTerminalStatus
    private String status;

    @Size(max = 50, message = "Serial number must not exceed 50 characters")
    private String serialNumber;

    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @Size(max = 50, message = "Manufacturer must not exceed 50 characters")
    private String manufacturer;

    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^$", message = "IP address must be a valid IPv4 format")
    private String ipAddress;
}