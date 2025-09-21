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
import com.rizki.edcmanagement.util.LoggingUtil;

@Service
public class TerminalEDCServiceImpl implements TerminalEDCService {
    @Autowired
    private TerminalEDCRepository terminalRepository;

    @Autowired
    private TerminalEDCMapper terminalEDCMapper;

    @Override
    @Transactional
    public TerminalEDCResponseDTO createTerminal(CreateTerminalEDCRequestDTO requestDTO) {
        long startTime = System.currentTimeMillis();

        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_CREATE_START",
                "terminalId", requestDTO.getTerminalId(),
                "location", requestDTO.getLocation(),
                "status", requestDTO.getStatus());

        try {
            // Business validation: Check terminal ID uniqueness (cannot be done in DTO)
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_VALIDATING_UNIQUENESS",
                    "terminalId", requestDTO.getTerminalId());

            if (terminalRepository.existsById(requestDTO.getTerminalId())) {
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_CREATE_FAILED_DUPLICATE_ID",
                        "terminalId", requestDTO.getTerminalId());
                throw new ResourceAlreadyExistsException(
                        "Terminal EDC with ID '" + requestDTO.getTerminalId() + "' already exists");
            }

            // Business validation: Check IP address uniqueness (cannot be done in DTO)
            if (requestDTO.getIpAddress() != null && !requestDTO.getIpAddress().trim().isEmpty()) {
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_VALIDATING_IP_UNIQUENESS",
                        "terminalId", requestDTO.getTerminalId(),
                        "ipAddress", requestDTO.getIpAddress());

                if (terminalRepository.existsByIpAddress(requestDTO.getIpAddress())) {
                    LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_CREATE_FAILED_DUPLICATE_IP",
                            "terminalId", requestDTO.getTerminalId(),
                            "ipAddress", requestDTO.getIpAddress());
                    throw new ResourceAlreadyExistsException(
                            "Terminal EDC with IP address '" + requestDTO.getIpAddress() + "' already exists");
                }
            }

            // Convert DTO to entity and save
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_MAPPING_ENTITY",
                    "terminalId", requestDTO.getTerminalId());
            TerminalEDC terminal = terminalEDCMapper.fromCreateRequestToTerminalEDC(requestDTO);

            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_SAVING_ENTITY",
                    "terminalId", requestDTO.getTerminalId());
            TerminalEDC savedTerminal = terminalRepository.save(terminal);

            TerminalEDCResponseDTO response = terminalEDCMapper.fromTerminalEDCToResponse(savedTerminal);

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_CREATE_SUCCESS",
                    "terminalId", savedTerminal.getTerminalId(),
                    "location", savedTerminal.getLocation(),
                    "status", savedTerminal.getStatus().toString(),
                    "createdAt", savedTerminal.getCreatedAt().toString(),
                    "duration", duration + "ms");

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_SERVICE_CREATE_ERROR", e,
                    "terminalId", requestDTO.getTerminalId(),
                    "location", requestDTO.getLocation(),
                    "duration", duration + "ms");
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedTerminalEDCResponseDTO getAllTerminals(GetTerminalEDCRequestDTO requestDTO) {
        long startTime = System.currentTimeMillis();

        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_GET_ALL_START",
                "page", String.valueOf(requestDTO.getPage()),
                "size", String.valueOf(requestDTO.getSize()),
                "sortBy", requestDTO.getSortBy(),
                "sortDirection", requestDTO.getSortDirection(),
                "status", requestDTO.getStatus(),
                "location", requestDTO.getLocation());

        try {
            // Build sorting
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_BUILDING_SORT",
                    "sortBy", requestDTO.getSortBy(),
                    "sortDirection", requestDTO.getSortDirection());
            Sort.Direction direction = "asc".equalsIgnoreCase(requestDTO.getSortDirection()) ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, requestDTO.getSortBy());

            // Build pageable
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_BUILDING_PAGEABLE",
                    "page", String.valueOf(requestDTO.getPage()),
                    "size", String.valueOf(requestDTO.getSize()));
            Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(), sort);

            // Build specification for filtering
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_BUILDING_SPECIFICATION",
                    "hasFilters", String.valueOf(hasFilters(requestDTO)));
            Specification<TerminalEDC> specification = TerminalEDCSpecification.buildSpecification(requestDTO);

            // Execute query
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_EXECUTING_QUERY",
                    "page", String.valueOf(requestDTO.getPage()),
                    "size", String.valueOf(requestDTO.getSize()));
            Page<TerminalEDC> terminalPage = terminalRepository.findAll(specification, pageable);

            // Convert entities to DTOs
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_MAPPING_RESULTS",
                    "resultCount", String.valueOf(terminalPage.getNumberOfElements()),
                    "totalElements", String.valueOf(terminalPage.getTotalElements()));
            List<TerminalEDCResponseDTO> terminalDTOs = terminalPage.getContent()
                    .stream()
                    .map(terminalEDCMapper::fromTerminalEDCToResponse)
                    .collect(Collectors.toList());

            // Build applied filters description
            String appliedFilters = TerminalEDCSpecification.buildAppliedFiltersDescription(requestDTO);

            // Build response
            PagedTerminalEDCResponseDTO response = PagedTerminalEDCResponseDTO.builder()
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

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_GET_ALL_SUCCESS",
                    "totalElements", String.valueOf(response.getTotalElements()),
                    "totalPages", String.valueOf(response.getTotalPages()),
                    "numberOfElements", String.valueOf(response.getNumberOfElements()),
                    "appliedFilters", appliedFilters,
                    "duration", duration + "ms");

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_SERVICE_GET_ALL_ERROR", e,
                    "page", String.valueOf(requestDTO.getPage()),
                    "size", String.valueOf(requestDTO.getSize()),
                    "duration", duration + "ms");
            throw e;
        }
    }

    private boolean hasFilters(GetTerminalEDCRequestDTO requestDTO) {
        return requestDTO.getStatus() != null ||
                requestDTO.getLocation() != null ||
                requestDTO.getManufacturer() != null ||
                requestDTO.getModel() != null ||
                requestDTO.getTerminalType() != null ||
                requestDTO.getIpAddress() != null ||
                requestDTO.getSerialNumber() != null ||
                requestDTO.getCreatedFrom() != null ||
                requestDTO.getCreatedTo() != null ||
                requestDTO.getLastMaintenanceFrom() != null ||
                requestDTO.getLastMaintenanceTo() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public TerminalEDCResponseDTO getTerminalById(String terminalId) {
        long startTime = System.currentTimeMillis();

        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_GET_BY_ID_START",
                "terminalId", terminalId);

        try {
            // Find terminal by ID
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_FINDING_BY_ID",
                    "terminalId", terminalId);
            TerminalEDC terminal = terminalRepository.findById(terminalId)
                    .orElseThrow(() -> {
                        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_GET_BY_ID_NOT_FOUND",
                                "terminalId", terminalId);
                        return new ResourceNotFoundException(
                                "Terminal EDC with ID '" + terminalId + "' not found");
                    });

            // Convert entity to DTO and return
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_MAPPING_RESPONSE",
                    "terminalId", terminalId,
                    "location", terminal.getLocation(),
                    "status", terminal.getStatus().toString());
            TerminalEDCResponseDTO response = terminalEDCMapper.fromTerminalEDCToResponse(terminal);

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_GET_BY_ID_SUCCESS",
                    "terminalId", terminal.getTerminalId(),
                    "location", terminal.getLocation(),
                    "status", terminal.getStatus().toString(),
                    "lastMaintenance",
                    terminal.getLastMaintenance() != null ? terminal.getLastMaintenance().toString() : "null",
                    "duration", duration + "ms");

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_SERVICE_GET_BY_ID_ERROR", e,
                    "terminalId", terminalId,
                    "duration", duration + "ms");
            throw e;
        }
    }

    @Override
    @Transactional
    public TerminalEDCResponseDTO updateTerminal(String terminalId, UpdateTerminalEDCRequestDTO requestDTO) {
        long startTime = System.currentTimeMillis();

        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATE_START",
                "terminalId", terminalId,
                "newLocation", requestDTO.getLocation(),
                "newStatus", requestDTO.getStatus(),
                "newIpAddress", requestDTO.getIpAddress());

        try {
            // Find existing terminal
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_FINDING_FOR_UPDATE",
                    "terminalId", terminalId);
            TerminalEDC existingTerminal = terminalRepository.findById(terminalId)
                    .orElseThrow(() -> {
                        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATE_NOT_FOUND",
                                "terminalId", terminalId);
                        return new ResourceNotFoundException(
                                "Terminal EDC with ID '" + terminalId + "' not found");
                    });

            // Log current state
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_CURRENT_STATE",
                    "terminalId", terminalId,
                    "currentLocation", existingTerminal.getLocation(),
                    "currentStatus", existingTerminal.getStatus().toString(),
                    "currentIpAddress", existingTerminal.getIpAddress());

            // Business validation: Check IP address uniqueness if provided and different
            // from current
            if (requestDTO.getIpAddress() != null && !requestDTO.getIpAddress().trim().isEmpty()) {
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_VALIDATING_IP_UPDATE",
                        "terminalId", terminalId,
                        "newIpAddress", requestDTO.getIpAddress(),
                        "currentIpAddress", existingTerminal.getIpAddress());

                if (!requestDTO.getIpAddress().equals(existingTerminal.getIpAddress()) &&
                        terminalRepository.existsByIpAddress(requestDTO.getIpAddress())) {
                    LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATE_FAILED_DUPLICATE_IP",
                            "terminalId", terminalId,
                            "ipAddress", requestDTO.getIpAddress());
                    throw new ResourceAlreadyExistsException(
                            "Terminal EDC with IP address '" + requestDTO.getIpAddress() + "' already exists");
                }
            }

            // Store current status for maintenance detection
            TerminalStatus oldStatus = existingTerminal.getStatus();
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_STATUS_CHANGE_DETECTION",
                    "terminalId", terminalId,
                    "oldStatus", oldStatus.toString(),
                    "newStatus", requestDTO.getStatus());

            // Update terminal with new data
            boolean hasChanges = false;
            if (requestDTO.getLocation() != null) {
                String newLocation = requestDTO.getLocation().trim();
                if (!newLocation.equals(existingTerminal.getLocation())) {
                    LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATING_LOCATION",
                            "terminalId", terminalId,
                            "oldLocation", existingTerminal.getLocation(),
                            "newLocation", newLocation);
                    existingTerminal.setLocation(newLocation);
                    hasChanges = true;
                }
            }

            // Update status if provided
            if (requestDTO.getStatus() != null && !requestDTO.getStatus().trim().isEmpty()) {
                try {
                    TerminalStatus status = TerminalStatus.valueOf(requestDTO.getStatus().toUpperCase().trim());
                    if (status != existingTerminal.getStatus()) {
                        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATING_STATUS",
                                "terminalId", terminalId,
                                "oldStatus", existingTerminal.getStatus().toString(),
                                "newStatus", status.toString());
                        existingTerminal.setStatus(status);
                        hasChanges = true;
                    }
                } catch (IllegalArgumentException e) {
                    LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_INVALID_STATUS",
                            "terminalId", terminalId,
                            "invalidStatus", requestDTO.getStatus());
                    // Invalid status will be caught by validation
                }
            }

            // Update other fields with similar logging pattern
            if (requestDTO.getSerialNumber() != null) {
                String newSerialNumber = requestDTO.getSerialNumber().trim();
                if (!newSerialNumber.equals(existingTerminal.getSerialNumber())) {
                    existingTerminal.setSerialNumber(newSerialNumber);
                    hasChanges = true;
                }
            }

            if (requestDTO.getModel() != null) {
                String newModel = requestDTO.getModel().trim();
                if (!newModel.equals(existingTerminal.getModel())) {
                    existingTerminal.setModel(newModel);
                    hasChanges = true;
                }
            }

            if (requestDTO.getManufacturer() != null) {
                String newManufacturer = requestDTO.getManufacturer().trim();
                if (!newManufacturer.equals(existingTerminal.getManufacturer())) {
                    existingTerminal.setManufacturer(newManufacturer);
                    hasChanges = true;
                }
            }

            if (requestDTO.getIpAddress() != null) {
                String ipAddress = requestDTO.getIpAddress().trim();
                String newIpAddress = ipAddress.isEmpty() ? null : ipAddress;
                if (!java.util.Objects.equals(newIpAddress, existingTerminal.getIpAddress())) {
                    existingTerminal.setIpAddress(newIpAddress);
                    hasChanges = true;
                }
            }

            // Auto-update lastMaintenance if status changed from MAINTENANCE to something
            // else
            if (oldStatus == TerminalStatus.MAINTENANCE &&
                    existingTerminal.getStatus() != TerminalStatus.MAINTENANCE) {
                LocalDateTime maintenanceTime = LocalDateTime.now();
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_AUTO_UPDATE_MAINTENANCE",
                        "terminalId", terminalId,
                        "maintenanceCompletedAt", maintenanceTime.toString());
                existingTerminal.setLastMaintenance(maintenanceTime);
                hasChanges = true;
            }

            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_CHANGES_DETECTED",
                    "terminalId", terminalId,
                    "hasChanges", String.valueOf(hasChanges));

            // Save updated terminal
            if (hasChanges) {
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_SAVING_UPDATES",
                        "terminalId", terminalId);
                TerminalEDC updatedTerminal = terminalRepository.save(existingTerminal);

                TerminalEDCResponseDTO response = terminalEDCMapper.fromTerminalEDCToResponse(updatedTerminal);

                long duration = System.currentTimeMillis() - startTime;
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATE_SUCCESS",
                        "terminalId", updatedTerminal.getTerminalId(),
                        "location", updatedTerminal.getLocation(),
                        "status", updatedTerminal.getStatus().toString(),
                        "lastMaintenance",
                        updatedTerminal.getLastMaintenance() != null ? updatedTerminal.getLastMaintenance().toString()
                                : "null",
                        "updatedAt", updatedTerminal.getUpdatedAt().toString(),
                        "duration", duration + "ms");

                return response;
            } else {
                TerminalEDCResponseDTO response = terminalEDCMapper.fromTerminalEDCToResponse(existingTerminal);

                long duration = System.currentTimeMillis() - startTime;
                LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_UPDATE_NO_CHANGES",
                        "terminalId", terminalId,
                        "duration", duration + "ms");

                return response;
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_SERVICE_UPDATE_ERROR", e,
                    "terminalId", terminalId,
                    "duration", duration + "ms");
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteTerminal(String terminalId) {
        long startTime = System.currentTimeMillis();

        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_DELETE_START",
                "terminalId", terminalId);

        try {
            // Find existing terminal to ensure it exists
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_FINDING_FOR_DELETE",
                    "terminalId", terminalId);
            TerminalEDC existingTerminal = terminalRepository.findById(terminalId)
                    .orElseThrow(() -> {
                        LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_DELETE_NOT_FOUND",
                                "terminalId", terminalId);
                        return new ResourceNotFoundException(
                                "Terminal EDC with ID '" + terminalId + "' not found");
                    });

            // Log terminal info before deletion
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_PRE_DELETE_INFO",
                    "terminalId", existingTerminal.getTerminalId(),
                    "location", existingTerminal.getLocation(),
                    "status", existingTerminal.getStatus().toString(),
                    "manufacturer", existingTerminal.getManufacturer(),
                    "model", existingTerminal.getModel(),
                    "ipAddress", existingTerminal.getIpAddress(),
                    "createdAt", existingTerminal.getCreatedAt().toString());

            // Delete the terminal
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_EXECUTING_DELETE",
                    "terminalId", terminalId);
            terminalRepository.delete(existingTerminal);

            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logBusinessEvent("TERMINAL_SERVICE_DELETE_SUCCESS",
                    "terminalId", terminalId,
                    "deletedLocation", existingTerminal.getLocation(),
                    "deletedStatus", existingTerminal.getStatus().toString(),
                    "duration", duration + "ms");

            // Log audit event for deletion
            LoggingUtil.logAuditEvent("TERMINAL_DELETED_BY_SERVICE",
                    "Terminal " + terminalId + " (Location: " + existingTerminal.getLocation() +
                            ", Status: " + existingTerminal.getStatus() + ") was successfully deleted");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LoggingUtil.logError("TERMINAL_SERVICE_DELETE_ERROR", e,
                    "terminalId", terminalId,
                    "duration", duration + "ms");
            throw e;
        }
    }
}