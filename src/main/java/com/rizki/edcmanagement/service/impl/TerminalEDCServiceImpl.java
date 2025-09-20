package com.rizki.edcmanagement.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;
import com.rizki.edcmanagement.exception.ResourceAlreadyExistsException;
import com.rizki.edcmanagement.mapper.TerminalEDCMapper;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.repository.TerminalEDCRepository;
import com.rizki.edcmanagement.service.TerminalEDCService;

@Service
public class TerminalEDCServiceImpl implements TerminalEDCService {
    @Autowired
    private TerminalEDCRepository terminalRepository;

    @Autowired
    private TerminalEDCMapper terminalEDCMapper;

    @Override
    @Transactional
    public TerminalEDCResponseDTO createTerminal(CreateTerminalEDCRequestDTO requestDTO) {
        // Business validation: Check terminal ID uniqueness (cannot be done in DTO)
        if (terminalRepository.existsById(requestDTO.getTerminalId())) {
            throw new ResourceAlreadyExistsException(
                    "Terminal EDC with ID '" + requestDTO.getTerminalId() + "' already exists");
        }

        // Business validation: Check IP address uniqueness (cannot be done in DTO)
        if (requestDTO.getIpAddress() != null && !requestDTO.getIpAddress().trim().isEmpty()) {
            if (terminalRepository.existsByIpAddress(requestDTO.getIpAddress())) {
                throw new ResourceAlreadyExistsException(
                        "Terminal EDC with IP address '" + requestDTO.getIpAddress() + "' already exists");
            }
        }

        // Convert DTO to entity and save
        TerminalEDC terminal = terminalEDCMapper.fromCreateRequestToTerminalEDC(requestDTO);
        TerminalEDC savedTerminal = terminalRepository.save(terminal);

        return terminalEDCMapper.fromTerminalEDCToResponse(savedTerminal);
    }
}