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
import com.rizki.edcmanagement.util.LoggingUtil;

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
        LoggingUtil.logBusinessEvent("ECHO_SERVICE_CREATE_STARTED",
                "TERMINAL_ID", requestDTO.getTerminalId());

        try {
            // Use current UTC time for signature validation
            Instant now = Instant.now();
            LocalDateTime requestTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

            LoggingUtil.logBusinessEvent("ECHO_SIGNATURE_VALIDATION_STARTED",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "REQUEST_TIME", requestTime);

            // Validate signature using current server timestamp
            boolean isValidSignature = signatureValidationService.validateSignatureWithTolerance(
                    signature, requestDTO.getTerminalId(), requestTime);

            if (!isValidSignature) {
                LoggingUtil.logBusinessEvent("ECHO_SIGNATURE_VALIDATION_FAILED",
                        "TERMINAL_ID", requestDTO.getTerminalId(),
                        "REASON", "Invalid signature");
                throw new InvalidSignatureException("Invalid signature");
            }

            LoggingUtil.logBusinessEvent("ECHO_SIGNATURE_VALIDATION_SUCCESS",
                    "TERMINAL_ID", requestDTO.getTerminalId());

            // Find terminal
            LoggingUtil.logBusinessEvent("ECHO_TERMINAL_LOOKUP_STARTED",
                    "TERMINAL_ID", requestDTO.getTerminalId());

            TerminalEDC terminal = terminalEDCRepository.findById(requestDTO.getTerminalId())
                    .orElseThrow(() -> {
                        LoggingUtil.logBusinessEvent("ECHO_TERMINAL_NOT_FOUND",
                                "TERMINAL_ID", requestDTO.getTerminalId());
                        return new ResourceNotFoundException("Terminal ID not found");
                    });

            LoggingUtil.logBusinessEvent("ECHO_TERMINAL_FOUND",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "TERMINAL_STATUS", terminal.getStatus());

            // Create echo log entry with current UTC instant - set timestamp manually
            EchoLog echoLog = EchoLog.builder()
                    .terminal(terminal)
                    .timestamp(now) // Set timestamp manually from server
                    .build();

            LoggingUtil.logBusinessEvent("ECHO_LOG_SAVE_STARTED",
                    "TERMINAL_ID", requestDTO.getTerminalId());

            // Save to database
            EchoLog saved = echoLogRepository.save(echoLog);

            LoggingUtil.logBusinessEvent("ECHO_LOG_SAVE_SUCCESS",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "ECHO_LOG_ID", saved.getId(),
                    "TIMESTAMP", saved.getTimestamp());

            // Return response - convert Instant to LocalDateTime for response
            EchoResponseDTO response = EchoResponseDTO.builder()
                    .id(saved.getId())
                    .terminalId(requestDTO.getTerminalId())
                    .timestamp(LocalDateTime.ofInstant(saved.getTimestamp(), ZoneOffset.UTC))
                    .build();

            LoggingUtil.logBusinessEvent("ECHO_SERVICE_CREATE_COMPLETED",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "ECHO_LOG_ID", saved.getId(),
                    "STATUS", "SUCCESS");

            return response;

        } catch (InvalidSignatureException | ResourceNotFoundException e) {
            LoggingUtil.logBusinessEvent("ECHO_SERVICE_CREATE_FAILED",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("ECHO_SERVICE_CREATE_ERROR",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedEchoLogResponseDTO getAllEchoLogs(GetEchoLogRequestDTO requestDTO) {
        LoggingUtil.logBusinessEvent("ECHO_LOGS_SERVICE_QUERY_STARTED",
                "TERMINAL_ID_FILTER", requestDTO.getTerminalId(),
                "PAGE", requestDTO.getPage(),
                "SIZE", requestDTO.getSize(),
                "SORT_BY", requestDTO.getSortBy(),
                "SORT_DIRECTION", requestDTO.getSortDirection());

        try {
            // Build sorting
            Sort.Direction direction = "asc".equalsIgnoreCase(requestDTO.getSortDirection()) ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, requestDTO.getSortBy());

            LoggingUtil.logBusinessEvent("ECHO_LOGS_SORT_CONFIGURED",
                    "SORT_FIELD", requestDTO.getSortBy(),
                    "SORT_DIRECTION", direction.toString());

            // Build pageable
            Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(), sort);

            LoggingUtil.logBusinessEvent("ECHO_LOGS_PAGINATION_CONFIGURED",
                    "PAGE", requestDTO.getPage(),
                    "SIZE", requestDTO.getSize());

            // Build specification for filtering
            Specification<EchoLog> specification = EchoLogSpecification.buildSpecification(requestDTO);

            LoggingUtil.logBusinessEvent("ECHO_LOGS_FILTERS_APPLIED",
                    "TERMINAL_ID_FILTER", requestDTO.getTerminalId(),
                    "TIMESTAMP_FROM", requestDTO.getTimestampFrom(),
                    "TIMESTAMP_TO", requestDTO.getTimestampTo());

            // Execute query
            LoggingUtil.logBusinessEvent("ECHO_LOGS_DATABASE_QUERY_STARTED");

            Page<EchoLog> echoLogPage = echoLogRepository.findAll(specification, pageable);

            LoggingUtil.logBusinessEvent("ECHO_LOGS_DATABASE_QUERY_COMPLETED",
                    "TOTAL_ELEMENTS", echoLogPage.getTotalElements(),
                    "TOTAL_PAGES", echoLogPage.getTotalPages(),
                    "NUMBER_OF_ELEMENTS", echoLogPage.getNumberOfElements());

            // Convert entities to DTOs
            LoggingUtil.logBusinessEvent("ECHO_LOGS_DTO_MAPPING_STARTED",
                    "RECORDS_TO_MAP", echoLogPage.getNumberOfElements());

            List<EchoResponseDTO> echoLogDTOs = echoLogPage.getContent()
                    .stream()
                    .map(echoLog -> EchoResponseDTO.builder()
                            .id(echoLog.getId())
                            .terminalId(echoLog.getTerminal().getTerminalId())
                            .timestamp(LocalDateTime.ofInstant(echoLog.getTimestamp(), ZoneOffset.UTC))
                            .build())
                    .collect(Collectors.toList());

            LoggingUtil.logBusinessEvent("ECHO_LOGS_DTO_MAPPING_COMPLETED",
                    "MAPPED_RECORDS", echoLogDTOs.size());

            // Build applied filters description
            String appliedFilters = EchoLogSpecification.buildAppliedFiltersDescription(requestDTO);

            LoggingUtil.logBusinessEvent("ECHO_LOGS_FILTERS_DESCRIPTION",
                    "APPLIED_FILTERS", appliedFilters);

            // Build response
            PagedEchoLogResponseDTO response = PagedEchoLogResponseDTO.builder()
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

            LoggingUtil.logBusinessEvent("ECHO_LOGS_SERVICE_QUERY_COMPLETED",
                    "RETURNED_RECORDS", response.getNumberOfElements(),
                    "TOTAL_AVAILABLE", response.getTotalElements(),
                    "STATUS", "SUCCESS");

            return response;

        } catch (Exception e) {
            LoggingUtil.logBusinessEvent("ECHO_LOGS_SERVICE_QUERY_ERROR",
                    "ERROR", e.getClass().getSimpleName(),
                    "MESSAGE", e.getMessage());
            throw e;
        }
    }
}