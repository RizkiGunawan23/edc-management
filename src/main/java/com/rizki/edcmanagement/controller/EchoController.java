package com.rizki.edcmanagement.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
    @Autowired
    private EchoLogService echoLogService;

    @PostMapping("/echo")
    public ResponseEntity<SuccessResponse<EchoResponseDTO>> echo(@RequestHeader("Signature") String signature,
            @Valid @RequestBody EchoRequestDTO requestDTO, HttpServletRequest request) {

        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        LoggingUtil.setMDC(correlationId, clientIp, "EchoController");

        try {
            LoggingUtil.logBusinessEvent("ECHO_REQUEST_RECEIVED",
                    "Echo request received for terminal: " + requestDTO.getTerminalId() +
                            ", clientIp: " + clientIp);

            long startTime = System.currentTimeMillis();
            EchoResponseDTO response = echoLogService.createEchoLog(signature, requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent("ECHO_REQUEST_PROCESSED",
                    "Echo request processed successfully for terminal: " + requestDTO.getTerminalId() +
                            ", processingTime: " + processingTime + "ms" +
                            ", status: SUCCESS");

            LoggingUtil.logPerformance("ECHO_REQUEST", processingTime);

            SuccessResponse<EchoResponseDTO> successResponse = SuccessResponse.<EchoResponseDTO>builder()
                    .message("Echo processed successfully")
                    .data(response)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
        } finally {
            LoggingUtil.clearMDC();
        }
    }

    @GetMapping("/echo-logs")
    public ResponseEntity<SuccessResponse<PagedEchoLogResponseDTO>> getAllEchoLogs(
            @Valid @ModelAttribute GetEchoLogRequestDTO requestDTO, HttpServletRequest request) {
        String correlationId = LoggingUtil.generateCorrelationId();
        String clientIp = LoggingUtil.getClientIpAddress(request);
        LoggingUtil.setMDC(correlationId, clientIp, "EchoController");

        try {
            LoggingUtil.logBusinessEvent("ECHO_LOGS_QUERY_STARTED",
                    "Echo logs query started with filters - terminalId: " + requestDTO.getTerminalId() +
                            ", page: " + requestDTO.getPage() +
                            ", size: " + requestDTO.getSize() +
                            ", clientIp: " + clientIp);

            long startTime = System.currentTimeMillis();
            PagedEchoLogResponseDTO responseDTO = echoLogService.getAllEchoLogs(requestDTO);
            long processingTime = System.currentTimeMillis() - startTime;

            LoggingUtil.logBusinessEvent("ECHO_LOGS_QUERY_COMPLETED",
                    "Echo logs query completed - recordsReturned: " + responseDTO.getNumberOfElements() +
                            ", totalRecords: " + responseDTO.getTotalElements() +
                            ", processingTime: " + processingTime + "ms");

            LoggingUtil.logPerformance("ECHO_LOGS_QUERY", processingTime);

            SuccessResponse<PagedEchoLogResponseDTO> response = SuccessResponse.<PagedEchoLogResponseDTO>builder()
                    .message("Echo logs retrieved successfully")
                    .data(responseDTO)
                    .build();
            return ResponseEntity.ok(response);
        } finally {
            LoggingUtil.clearMDC();
        }
    }
}