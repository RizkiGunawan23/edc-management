package com.rizki.edcmanagement.mapper;

import org.springframework.stereotype.Component;

import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.request.UpdateTerminalEDCRequestDTO;
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

    public void updateTerminalFromDTO(UpdateTerminalEDCRequestDTO requestDTO, TerminalEDC existingTerminal) {
        // Update location if provided
        if (requestDTO.getLocation() != null) {
            existingTerminal.setLocation(requestDTO.getLocation().trim());
        }

        // Update status if provided
        if (requestDTO.getStatus() != null && !requestDTO.getStatus().trim().isEmpty()) {
            try {
                TerminalStatus status = TerminalStatus.valueOf(requestDTO.getStatus().toUpperCase().trim());
                existingTerminal.setStatus(status);
            } catch (IllegalArgumentException e) {
                // Invalid status will be caught by validation
            }
        }

        // Update serial number if provided
        if (requestDTO.getSerialNumber() != null) {
            existingTerminal.setSerialNumber(requestDTO.getSerialNumber().trim());
        }

        // Update model if provided
        if (requestDTO.getModel() != null) {
            existingTerminal.setModel(requestDTO.getModel().trim());
        }

        // Update manufacturer if provided
        if (requestDTO.getManufacturer() != null) {
            existingTerminal.setManufacturer(requestDTO.getManufacturer().trim());
        }

        // Update IP address if provided
        if (requestDTO.getIpAddress() != null) {
            String ipAddress = requestDTO.getIpAddress().trim();
            existingTerminal.setIpAddress(ipAddress.isEmpty() ? null : ipAddress);
        }
    }
}