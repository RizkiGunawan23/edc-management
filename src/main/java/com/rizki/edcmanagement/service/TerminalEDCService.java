package com.rizki.edcmanagement.service;

import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;

public interface TerminalEDCService {
    TerminalEDCResponseDTO createTerminal(CreateTerminalEDCRequestDTO requestDTO);
}