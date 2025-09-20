package com.rizki.edcmanagement.dto.terminal.response;

import java.time.LocalDateTime;

import com.rizki.edcmanagement.model.enums.TerminalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalEDCResponseDTO {
    private String terminalId;

    private String location;

    private TerminalStatus status;

    private String serialNumber;

    private String model;

    private String manufacturer;

    private LocalDateTime lastMaintenance;

    private String ipAddress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}