package com.rizki.edcmanagement.dto.terminal.request;

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
public class UpdateTerminalEDCRequestDTO {
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

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