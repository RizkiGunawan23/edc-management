package com.rizki.edcmanagement.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.echo.request.EchoRequestDTO;
import com.rizki.edcmanagement.dto.echo.request.GetEchoLogRequestDTO;
import com.rizki.edcmanagement.dto.echo.response.EchoResponseDTO;
import com.rizki.edcmanagement.dto.echo.response.PagedEchoLogResponseDTO;
import com.rizki.edcmanagement.exception.InvalidSignatureException;
import com.rizki.edcmanagement.exception.ResourceNotFoundException;
import com.rizki.edcmanagement.model.EchoLog;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.repository.EchoLogRepository;
import com.rizki.edcmanagement.repository.TerminalEDCRepository;
import com.rizki.edcmanagement.service.EchoLogService;
import com.rizki.edcmanagement.service.SignatureValidationService;
import com.rizki.edcmanagement.specification.EchoLogSpecification;

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

    @Override
    @Transactional(readOnly = true)
    public PagedEchoLogResponseDTO getAllEchoLogs(GetEchoLogRequestDTO requestDTO) {
        // Build sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(requestDTO.getSortDirection()) ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, requestDTO.getSortBy());

        // Build pageable
        Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(), sort);

        // Build specification for filtering
        Specification<EchoLog> specification = EchoLogSpecification.buildSpecification(requestDTO);

        // Execute query
        Page<EchoLog> echoLogPage = echoLogRepository.findAll(specification, pageable);

        // Convert entities to DTOs
        List<EchoResponseDTO> echoLogDTOs = echoLogPage.getContent()
                .stream()
                .map(echoLog -> EchoResponseDTO.builder()
                        .id(echoLog.getId())
                        .terminalId(echoLog.getTerminal().getTerminalId())
                        .timestamp(LocalDateTime.ofInstant(echoLog.getTimestamp(), ZoneOffset.UTC))
                        .build())
                .collect(Collectors.toList());

        // Build applied filters description
        String appliedFilters = EchoLogSpecification.buildAppliedFiltersDescription(requestDTO);

        // Build response
        return PagedEchoLogResponseDTO.builder()
                .echoLogs(echoLogDTOs)
                .page(echoLogPage.getNumber())
                .size(echoLogPage.getSize())
                .totalElements(echoLogPage.getTotalElements())
                .totalPages(echoLogPage.getTotalPages())
                .first(echoLogPage.isFirst())
                .last(echoLogPage.isLast())
                .hasNext(echoLogPage.hasNext())
                .hasPrevious(echoLogPage.hasPrevious())
                .numberOfElements(echoLogPage.getNumberOfElements())
                .sortBy(requestDTO.getSortBy())
                .sortDirection(requestDTO.getSortDirection())
                .appliedFilters(appliedFilters)
                .build();
    }
}