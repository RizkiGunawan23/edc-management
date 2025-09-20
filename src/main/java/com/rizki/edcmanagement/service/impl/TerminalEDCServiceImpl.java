package com.rizki.edcmanagement.service.impl;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.terminal.request.CreateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.request.GetTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.request.UpdateTerminalEDCRequestDTO;
import com.rizki.edcmanagement.dto.terminal.response.PagedTerminalEDCResponseDTO;
import com.rizki.edcmanagement.dto.terminal.response.TerminalEDCResponseDTO;
import com.rizki.edcmanagement.exception.ResourceAlreadyExistsException;
import com.rizki.edcmanagement.exception.ResourceNotFoundException;
import com.rizki.edcmanagement.mapper.TerminalEDCMapper;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.model.enums.TerminalStatus;
import com.rizki.edcmanagement.repository.TerminalEDCRepository;
import com.rizki.edcmanagement.service.TerminalEDCService;
import com.rizki.edcmanagement.specification.TerminalEDCSpecification;

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

    @Override
    @Transactional(readOnly = true)
    public PagedTerminalEDCResponseDTO getAllTerminals(GetTerminalEDCRequestDTO requestDTO) {
        // Build sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(requestDTO.getSortDirection()) ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, requestDTO.getSortBy());

        // Build pageable
        Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(), sort);

        // Build specification for filtering
        Specification<TerminalEDC> specification = TerminalEDCSpecification.buildSpecification(requestDTO);

        // Execute query
        Page<TerminalEDC> terminalPage = terminalRepository.findAll(specification, pageable);

        // Convert entities to DTOs
        List<TerminalEDCResponseDTO> terminalDTOs = terminalPage.getContent()
                .stream()
                .map(terminalEDCMapper::fromTerminalEDCToResponse)
                .collect(Collectors.toList());

        // Build applied filters description
        String appliedFilters = TerminalEDCSpecification.buildAppliedFiltersDescription(requestDTO);

        // Build response
        return PagedTerminalEDCResponseDTO.builder()
                .terminalEDC(terminalDTOs)
                .page(terminalPage.getNumber())
                .size(terminalPage.getSize())
                .totalElements(terminalPage.getTotalElements())
                .totalPages(terminalPage.getTotalPages())
                .first(terminalPage.isFirst())
                .last(terminalPage.isLast())
                .hasNext(terminalPage.hasNext())
                .hasPrevious(terminalPage.hasPrevious())
                .numberOfElements(terminalPage.getNumberOfElements())
                .sortBy(requestDTO.getSortBy())
                .sortDirection(requestDTO.getSortDirection())
                .appliedFilters(appliedFilters)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TerminalEDCResponseDTO getTerminalById(String terminalId) {
        // Find terminal by ID
        TerminalEDC terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Terminal EDC with ID '" + terminalId + "' not found"));

        // Convert entity to DTO and return
        return terminalEDCMapper.fromTerminalEDCToResponse(terminal);
    }

    @Override
    @Transactional
    public TerminalEDCResponseDTO updateTerminal(String terminalId, UpdateTerminalEDCRequestDTO requestDTO) {
        // Find existing terminal
        TerminalEDC existingTerminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Terminal EDC with ID '" + terminalId + "' not found"));

        // Business validation: Check IP address uniqueness if provided and different
        // from current
        if (requestDTO.getIpAddress() != null && !requestDTO.getIpAddress().trim().isEmpty()) {
            if (!requestDTO.getIpAddress().equals(existingTerminal.getIpAddress()) &&
                    terminalRepository.existsByIpAddress(requestDTO.getIpAddress())) {
                throw new ResourceAlreadyExistsException(
                        "Terminal EDC with IP address '" + requestDTO.getIpAddress() + "' already exists");
            }
        }

        // Store current status for maintenance detection
        TerminalStatus oldStatus = existingTerminal.getStatus();

        // Update terminal with new data
        if (requestDTO.getLocation() != null) {
            existingTerminal.setLocation(requestDTO.getLocation().trim());
        }

        // Update status if provided
        if (requestDTO.getStatus() != null && !requestDTO.getStatus().trim().isEmpty()) {
            try {
                TerminalStatus status = TerminalStatus.valueOf(requestDTO.getStatus().toUpperCase().trim());
                existingTerminal.setStatus(status);
            } catch (IllegalArgumentException e) {
                // Invalid status will be caught by validation
            }
        }

        // Update serial number if provided
        if (requestDTO.getSerialNumber() != null) {
            existingTerminal.setSerialNumber(requestDTO.getSerialNumber().trim());
        }

        // Update model if provided
        if (requestDTO.getModel() != null) {
            existingTerminal.setModel(requestDTO.getModel().trim());
        }

        // Update manufacturer if provided
        if (requestDTO.getManufacturer() != null) {
            existingTerminal.setManufacturer(requestDTO.getManufacturer().trim());
        }

        // Update IP address if provided
        if (requestDTO.getIpAddress() != null) {
            String ipAddress = requestDTO.getIpAddress().trim();
            existingTerminal.setIpAddress(ipAddress.isEmpty() ? null : ipAddress);
        }

        // Auto-update lastMaintenance if status changed from MAINTENANCE to something
        // else
        if (oldStatus == TerminalStatus.MAINTENANCE &&
                existingTerminal.getStatus() != TerminalStatus.MAINTENANCE) {
            existingTerminal.setLastMaintenance(LocalDateTime.now());
        }

        // Save updated terminal
        TerminalEDC updatedTerminal = terminalRepository.save(existingTerminal);

        // Convert entity to DTO and return
        return terminalEDCMapper.fromTerminalEDCToResponse(updatedTerminal);
    }
}