package com.rizki.edcmanagement.service;

import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.request.GetTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.PagedTerminalEDCResponseDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;

public interface TerminalEDCService {
    TerminalEDCResponseDTO createTerminal(CreateTerminalEDCRequestDTO requestDTO);

    PagedTerminalEDCResponseDTO getAllTerminals(GetTerminalEDCRequestDTO requestDTO);

    TerminalEDCResponseDTO getTerminalById(String terminalId);
}