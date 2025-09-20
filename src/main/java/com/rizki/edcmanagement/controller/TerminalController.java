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

    @GetMapping
    public ResponseEntity<SuccessResponse<PagedTerminalEDCResponseDTO>> getAllTerminals(
            @Valid @ModelAttribute GetTerminalEDCRequestDTO requestDTO) {
        PagedTerminalEDCResponseDTO responseDTO = terminalEDCService.getAllTerminals(requestDTO);
        SuccessResponse<PagedTerminalEDCResponseDTO> response = SuccessResponse.<PagedTerminalEDCResponseDTO>builder()
                .message("Terminals retrieved successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{terminalId}")
    public ResponseEntity<SuccessResponse<TerminalEDCResponseDTO>> getTerminalById(
            @PathVariable String terminalId) {
        TerminalEDCResponseDTO responseDTO = terminalEDCService.getTerminalById(terminalId);
        SuccessResponse<TerminalEDCResponseDTO> response = SuccessResponse.<TerminalEDCResponseDTO>builder()
                .message("Terminal retrieved successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{terminalId}")
    public ResponseEntity<SuccessResponse<TerminalEDCResponseDTO>> updateTerminal(
            @PathVariable String terminalId,
            @Valid @RequestBody UpdateTerminalEDCRequestDTO requestDTO) {
        TerminalEDCResponseDTO responseDTO = terminalEDCService.updateTerminal(terminalId, requestDTO);
        SuccessResponse<TerminalEDCResponseDTO> response = SuccessResponse.<TerminalEDCResponseDTO>builder()
                .message("Terminal updated successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{terminalId}")
    public ResponseEntity<Void> deleteTerminal(@PathVariable String terminalId) {
        terminalEDCService.deleteTerminal(terminalId);
        return ResponseEntity.noContent().build();
    }
}