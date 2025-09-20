package com.rizki.edcmanagement.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.echo.request.EchoRequestDTO;
import com.rizki.edcmanagement.dto.echo.response.EchoResponseDTO;
import com.rizki.edcmanagement.exception.InvalidSignatureException;
import com.rizki.edcmanagement.exception.ResourceNotFoundException;
import com.rizki.edcmanagement.model.EchoLog;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.repository.EchoLogRepository;
import com.rizki.edcmanagement.repository.TerminalEDCRepository;
import com.rizki.edcmanagement.service.EchoLogService;
import com.rizki.edcmanagement.service.SignatureValidationService;

@Service
public class EchoLogServiceImpl implements EchoLogService {
    @Autowired
    private EchoLogRepository echoLogRepository;

    @Autowired
    private TerminalEDCRepository terminalEDCRepository;

    @Autowired
    private SignatureValidationService signatureValidationService;

    @Override
    @Transactional
    public EchoResponseDTO createEchoLog(String signature, EchoRequestDTO requestDTO) {
        // Use current UTC time for signature validation
        Instant now = Instant.now();
        LocalDateTime requestTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

        // Validate signature using current server timestamp
        boolean isValidSignature = signatureValidationService.validateSignatureWithTolerance(
                signature, requestDTO.getTerminalId(), requestTime);

        if (!isValidSignature) {
            throw new InvalidSignatureException("Invalid signature");
        }

        // Find terminal
        TerminalEDC terminal = terminalEDCRepository.findById(requestDTO.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Terminal ID not found"));

        // Create echo log entry with current UTC instant - set timestamp manually
        EchoLog echoLog = EchoLog.builder()
                .terminal(terminal)
                .timestamp(now) // Set timestamp manually from server
                .build();

        // Save to database
        EchoLog saved = echoLogRepository.save(echoLog);

        // Return response - convert Instant to LocalDateTime for response
        return EchoResponseDTO.builder()
                .id(saved.getId())
                .terminalId(requestDTO.getTerminalId())
                .timestamp(LocalDateTime.ofInstant(saved.getTimestamp(), ZoneOffset.UTC))
                .build();
    }
}