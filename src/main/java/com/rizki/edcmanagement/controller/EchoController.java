package com.rizki.edcmanagement.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizki.edcmanagement.dto.common.SuccessResponse;
import com.rizki.edcmanagement.dto.echo.request.EchoRequestDTO;
import com.rizki.edcmanagement.dto.echo.request.GetEchoLogRequestDTO;
import com.rizki.edcmanagement.dto.echo.response.EchoResponseDTO;
import com.rizki.edcmanagement.dto.echo.response.PagedEchoLogResponseDTO;
import com.rizki.edcmanagement.service.EchoLogService;
import com.rizki.edcmanagement.util.LoggingUtil;

@RestController
@RequestMapping("/api/edc")
public class EchoController {
    private static final Logger logger = LoggerFactory.getLogger(EchoController.class);

    @Autowired
    private EchoLogService echoLogService;

    @PostMapping("/echo")
    public ResponseEntity<SuccessResponse<EchoResponseDTO>> echo(@RequestHeader("Signature") String signature,
            @Valid @RequestBody EchoRequestDTO requestDTO) {

        // Set correlation context untuk tracking
        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        LoggingUtil.setCorrelationContext(correlationId, requestDTO.getTerminalId(), "ECHO_REQUEST");

        try {
            LoggingUtil.logBusinessEvent(logger, "ECHO_REQUEST_RECEIVED",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "CORRELATION_ID", correlationId);

            long startTime = System.currentTimeMillis();
            EchoResponseDTO response = echoLogService.createEchoLog(signature, requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent(logger, "ECHO_REQUEST_PROCESSED",
                    "TERMINAL_ID", requestDTO.getTerminalId(),
                    "PROCESSING_TIME_MS", processingTime,
                    "STATUS", "SUCCESS");

            LoggingUtil.logPerformance("ECHO_REQUEST", processingTime);

            SuccessResponse<EchoResponseDTO> successResponse = SuccessResponse.<EchoResponseDTO>builder()
                    .message("Echo processed successfully")
                    .data(response)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
        } finally {
            LoggingUtil.clearCorrelationContext();
        }
    }

    @GetMapping("/echo-logs")
    public ResponseEntity<SuccessResponse<PagedEchoLogResponseDTO>> getAllEchoLogs(
            @Valid @ModelAttribute GetEchoLogRequestDTO requestDTO) {
        String correlationId = java.util.UUID.randomUUID().toString().substring(0, 8);
        LoggingUtil.setCorrelationContext(correlationId, requestDTO.getTerminalId(), "ECHO_LOGS_QUERY");

        try {
            LoggingUtil.logBusinessEvent(logger, "ECHO_LOGS_QUERY_STARTED",
                    "TERMINAL_ID_FILTER", requestDTO.getTerminalId(),
                    "PAGE", requestDTO.getPage(),
                    "SIZE", requestDTO.getSize(),
                    "CORRELATION_ID", correlationId);

            long startTime = System.currentTimeMillis();
            PagedEchoLogResponseDTO responseDTO = echoLogService.getAllEchoLogs(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent(logger, "ECHO_LOGS_QUERY_COMPLETED",
                    "RECORDS_RETURNED", responseDTO.getNumberOfElements(),
                    "TOTAL_RECORDS", responseDTO.getTotalElements(),
                    "PROCESSING_TIME_MS", processingTime);

            LoggingUtil.logPerformance("ECHO_LOGS_QUERY", processingTime);

            SuccessResponse<PagedEchoLogResponseDTO> response = SuccessResponse.<PagedEchoLogResponseDTO>builder()
                    .message("Echo logs retrieved successfully")
                    .data(responseDTO)
                    .build();
            return ResponseEntity.ok(response);
        } finally {
            LoggingUtil.clearCorrelationContext();
        }
    }
}