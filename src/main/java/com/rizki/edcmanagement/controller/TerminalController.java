package com.rizki.edcmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizki.edcmanagement.dto.common.SuccessResponse;
import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;
import com.rizki.edcmanagement.service.TerminalEDCService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/edc")
public class TerminalController {
    @Autowired
    private TerminalEDCService terminalEDCService;

    @PostMapping
    public ResponseEntity<SuccessResponse<TerminalEDCResponseDTO>> createTerminal(
            @Valid @RequestBody CreateTerminalEDCRequestDTO requestDTO) {
        TerminalEDCResponseDTO responseDTO = terminalEDCService.createTerminal(requestDTO);
        SuccessResponse<TerminalEDCResponseDTO> response = SuccessResponse.<TerminalEDCResponseDTO>builder()
                .message("Terminal created successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}