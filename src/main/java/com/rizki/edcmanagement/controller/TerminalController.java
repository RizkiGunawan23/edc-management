package com.rizki.edcmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizki.edcmanagement.dto.common.SuccessResponse;
import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.request.GetTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.request.UpdateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.PagedTerminalEDCResponseDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;
import com.rizki.edcmanagement.service.TerminalEDCService;
import com.rizki.edcmanagement.util.LoggingUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/edc")
public class TerminalController {
    @Autowired
    private TerminalEDCService terminalEDCService;

    @PostMapping
    public ResponseEntity<SuccessResponse<TerminalEDCResponseDTO>> createTerminal(
            @Valid @RequestBody CreateTerminalEDCRequestDTO requestDTO,
            HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        long startTime = System.currentTimeMillis();

        LoggingUtil.setMDC(correlationId, requestDTO.getTerminalId(), clientIp);
        LoggingUtil.logBusinessEvent("TERMINAL_CREATE_START",
                "terminalId", requestDTO.getTerminalId(),
                "location", requestDTO.getLocation(),
                "clientIp", clientIp);

        try {
            TerminalEDCResponseDTO responseDTO = terminalEDCService.createTerminal(requestDTO);
            SuccessResponse<TerminalEDCResponseDTO> response = SuccessResponse.<TerminalEDCResponseDTO>builder()
                    .message("Terminal created successfully")
                    .data(responseDTO)
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logPerformance("TERMINAL_CREATE", duration);
            LoggingUtil.logBusinessEvent("TERMINAL_CREATE_SUCCESS",
                    "terminalId", responseDTO.getTerminalId(),
                    "location", responseDTO.getLocation(),
                    "status", responseDTO.getStatus().toString(),
                    "duration", duration + "ms");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_CREATE_ERROR", e,
                    "terminalId", requestDTO.getTerminalId(),
                    "location", requestDTO.getLocation(),
                    "duration", duration + "ms");
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PagedTerminalEDCResponseDTO>> getAllTerminals(
            @Valid @ModelAttribute GetTerminalEDCRequestDTO requestDTO,
            HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        long startTime = System.currentTimeMillis();

        LoggingUtil.setMDC(correlationId, null, clientIp);
        LoggingUtil.logBusinessEvent("TERMINAL_GET_ALL_START",
                "page", String.valueOf(requestDTO.getPage()),
                "size", String.valueOf(requestDTO.getSize()),
                "sortBy", requestDTO.getSortBy(),
                "status", requestDTO.getStatus(),
                "location", requestDTO.getLocation(),
                "clientIp", clientIp);

        try {
            PagedTerminalEDCResponseDTO responseDTO = terminalEDCService.getAllTerminals(requestDTO);
            SuccessResponse<PagedTerminalEDCResponseDTO> response = SuccessResponse
                    .<PagedTerminalEDCResponseDTO>builder()
                    .message("Terminals retrieved successfully")
                    .data(responseDTO)
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logPerformance("TERMINAL_GET_ALL", duration);
            LoggingUtil.logBusinessEvent("TERMINAL_GET_ALL_SUCCESS",
                    "totalElements", String.valueOf(responseDTO.getTotalElements()),
                    "totalPages", String.valueOf(responseDTO.getTotalPages()),
                    "page", String.valueOf(responseDTO.getPage()),
                    "size", String.valueOf(responseDTO.getSize()),
                    "numberOfElements", String.valueOf(responseDTO.getNumberOfElements()),
                    "duration", duration + "ms");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_GET_ALL_ERROR", e,
                    "page", String.valueOf(requestDTO.getPage()),
                    "size", String.valueOf(requestDTO.getSize()),
                    "duration", duration + "ms");
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @GetMapping("/{terminalId}")
    public ResponseEntity<SuccessResponse<TerminalEDCResponseDTO>> getTerminalById(
            @PathVariable String terminalId,
            HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        long startTime = System.currentTimeMillis();

        LoggingUtil.setMDC(correlationId, terminalId, clientIp);
        LoggingUtil.logBusinessEvent("TERMINAL_GET_BY_ID_START",
                "terminalId", terminalId,
                "clientIp", clientIp);

        try {
            TerminalEDCResponseDTO responseDTO = terminalEDCService.getTerminalById(terminalId);
            SuccessResponse<TerminalEDCResponseDTO> response = SuccessResponse.<TerminalEDCResponseDTO>builder()
                    .message("Terminal retrieved successfully")
                    .data(responseDTO)
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logPerformance("TERMINAL_GET_BY_ID", duration);
            LoggingUtil.logBusinessEvent("TERMINAL_GET_BY_ID_SUCCESS",
                    "terminalId", responseDTO.getTerminalId(),
                    "location", responseDTO.getLocation(),
                    "status", responseDTO.getStatus().toString(),
                    "duration", duration + "ms");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_GET_BY_ID_ERROR", e,
                    "terminalId", terminalId,
                    "duration", duration + "ms");
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @PutMapping("/{terminalId}")
    public ResponseEntity<SuccessResponse<TerminalEDCResponseDTO>> updateTerminal(
            @PathVariable String terminalId,
            @Valid @RequestBody UpdateTerminalEDCRequestDTO requestDTO,
            HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        long startTime = System.currentTimeMillis();

        LoggingUtil.setMDC(correlationId, terminalId, clientIp);
        LoggingUtil.logBusinessEvent("TERMINAL_UPDATE_START",
                "terminalId", terminalId,
                "newLocation", requestDTO.getLocation(),
                "newStatus", requestDTO.getStatus(),
                "clientIp", clientIp);

        try {
            TerminalEDCResponseDTO responseDTO = terminalEDCService.updateTerminal(terminalId, requestDTO);
            SuccessResponse<TerminalEDCResponseDTO> response = SuccessResponse.<TerminalEDCResponseDTO>builder()
                    .message("Terminal updated successfully")
                    .data(responseDTO)
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logPerformance("TERMINAL_UPDATE", duration);
            LoggingUtil.logBusinessEvent("TERMINAL_UPDATE_SUCCESS",
                    "terminalId", responseDTO.getTerminalId(),
                    "location", responseDTO.getLocation(),
                    "status", responseDTO.getStatus().toString(),
                    "duration", duration + "ms");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_UPDATE_ERROR", e,
                    "terminalId", terminalId,
                    "duration", duration + "ms");
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @DeleteMapping("/{terminalId}")
    public ResponseEntity<Void> deleteTerminal(
            @PathVariable String terminalId,
            HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        long startTime = System.currentTimeMillis();

        LoggingUtil.setMDC(correlationId, terminalId, clientIp);
        LoggingUtil.logBusinessEvent("TERMINAL_DELETE_START",
                "terminalId", terminalId,
                "clientIp", clientIp);

        try {
            terminalEDCService.deleteTerminal(terminalId);

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logPerformance("TERMINAL_DELETE", duration);
            LoggingUtil.logBusinessEvent("TERMINAL_DELETE_SUCCESS",
                    "terminalId", terminalId,
                    "duration", duration + "ms");

            LoggingUtil.logAuditEvent("TERMINAL_DELETED",
                    "Terminal " + terminalId + " was deleted by client " + clientIp);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_DELETE_ERROR", e,
                    "terminalId", terminalId,
                    "duration", duration + "ms");
            throw e;
        } finally {
            LoggingUtil.clearMDC();
        }
    }
}