package com.rizki.edcmanagement.controller;

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

@RestController
@RequestMapping("/api/edc")
public class EchoController {
    @Autowired
    private EchoLogService echoLogService;

    @PostMapping("/echo")
    public ResponseEntity<SuccessResponse<EchoResponseDTO>> echo(@RequestHeader("Signature") String signature,
            @Valid @RequestBody EchoRequestDTO requestDTO) {
        EchoResponseDTO response = echoLogService.createEchoLog(signature, requestDTO);
        SuccessResponse<EchoResponseDTO> successResponse = SuccessResponse.<EchoResponseDTO>builder()
                .message("Echo processed successfully")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }

    @GetMapping("/echo-logs")
    public ResponseEntity<SuccessResponse<PagedEchoLogResponseDTO>> getAllEchoLogs(
            @Valid @ModelAttribute GetEchoLogRequestDTO requestDTO) {
        PagedEchoLogResponseDTO responseDTO = echoLogService.getAllEchoLogs(requestDTO);
        SuccessResponse<PagedEchoLogResponseDTO> response = SuccessResponse.<PagedEchoLogResponseDTO>builder()
                .message("Echo logs retrieved successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }
}