package com.rizki.edcmanagement.mapper;

import org.springframework.stereotype.Component;

import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.model.enums.TerminalStatus;

@Component
public class TerminalEDCMapper {
    public TerminalEDC fromCreateRequestToTerminalEDC(CreateTerminalEDCRequestDTO requestDTO) {
        TerminalEDC.TerminalEDCBuilder builder = TerminalEDC.builder()
                .terminalId(requestDTO.getTerminalId())
                .location(requestDTO.getLocation())
                .serialNumber(requestDTO.getSerialNumber())
                .model(requestDTO.getModel())
                .manufacturer(requestDTO.getManufacturer())
                .ipAddress(requestDTO.getIpAddress());

        // Convert String status to TerminalStatus enum
        if (requestDTO.getStatus() != null && !requestDTO.getStatus().trim().isEmpty()) {
            try {
                TerminalStatus status = TerminalStatus.valueOf(requestDTO.getStatus().toUpperCase().trim());
                builder.status(status);
            } catch (IllegalArgumentException e) {
            }
        }

        return builder.build();
    }

    public TerminalEDCResponseDTO fromTerminalEDCToResponse(TerminalEDC terminal) {
        return TerminalEDCResponseDTO.builder()
                .terminalId(terminal.getTerminalId())
                .location(terminal.getLocation())
                .status(terminal.getStatus())
                .serialNumber(terminal.getSerialNumber())
                .model(terminal.getModel())
                .manufacturer(terminal.getManufacturer())
                .lastMaintenance(terminal.getLastMaintenance())
                .ipAddress(terminal.getIpAddress())
                .createdAt(terminal.getCreatedAt())
                .updatedAt(terminal.getUpdatedAt())
                .build();
    }
}