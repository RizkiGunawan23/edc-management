package com.rizki.edcmanagement.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.rizki.edcmanagement.dto.terminal.request.GetTerminalEDCRequestDTO;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.model.enums.TerminalStatus;
import com.rizki.edcmanagement.util.LoggingUtil;

public class TerminalEDCSpecification {
    public static Specification<TerminalEDC> buildSpecification(GetTerminalEDCRequestDTO filters) {
        LoggingUtil.logBusinessEvent("TERMINAL_SPEC_BUILD_START",
                "hasStatus", String.valueOf(filters.getStatus() != null),
                "hasLocation", String.valueOf(filters.getLocation() != null),
                "hasManufacturer", String.valueOf(filters.getManufacturer() != null),
                "hasModel", String.valueOf(filters.getModel() != null),
                "hasTerminalType", String.valueOf(filters.getTerminalType() != null),
                "hasIpAddress", String.valueOf(filters.getIpAddress() != null));

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            int predicateCount = 0;

            // Filter by status
            if (filters.getStatus() != null && !filters.getStatus().trim().isEmpty()) {
                try {
                    TerminalStatus status = TerminalStatus.valueOf(filters.getStatus().toUpperCase().trim());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                    predicateCount++;
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_STATUS_FILTER",
                            "status", status.toString());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore this filter
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_INVALID_STATUS_FILTER",
                            "invalidStatus", filters.getStatus());
                }
            }

            // Filter by location (contains search, case insensitive)
            if (filters.getLocation() != null && !filters.getLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + filters.getLocation().toLowerCase().trim() + "%"));
                predicateCount++;
                LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_LOCATION_FILTER",
                        "location", filters.getLocation());
            }

            // Filter by manufacturer (contains search, case insensitive)
            if (filters.getManufacturer() != null && !filters.getManufacturer().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("manufacturer")),
                        "%" + filters.getManufacturer().toLowerCase().trim() + "%"));
                predicateCount++;
                LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_MANUFACTURER_FILTER",
                        "manufacturer", filters.getManufacturer());
            }

            // Filter by model (contains search, case insensitive)
            if (filters.getModel() != null && !filters.getModel().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("model")),
                        "%" + filters.getModel().toLowerCase().trim() + "%"));
                predicateCount++;
                LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_MODEL_FILTER",
                        "model", filters.getModel());
            }

            // Filter by terminal type (extracted from terminalId)
            if (filters.getTerminalType() != null && !filters.getTerminalType().trim().isEmpty()) {
                String terminalType = filters.getTerminalType().toUpperCase().trim();
                predicates.add(criteriaBuilder.like(
                        root.get("terminalId"),
                        terminalType + "-%"));
                predicateCount++;
                LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_TYPE_FILTER",
                        "terminalType", terminalType);
            }

            // Filter by IP address (exact match)
            if (filters.getIpAddress() != null && !filters.getIpAddress().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("ipAddress"), filters.getIpAddress().trim()));
                predicateCount++;
                LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_IP_FILTER",
                        "ipAddress", filters.getIpAddress());
            }

            // Filter by serial number (contains search, case insensitive)
            if (filters.getSerialNumber() != null && !filters.getSerialNumber().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serialNumber")),
                        "%" + filters.getSerialNumber().toLowerCase().trim() + "%"));
                predicateCount++;
                LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_SERIAL_FILTER",
                        "serialNumber", filters.getSerialNumber());
            }

            // Filter by created date range
            if (filters.getCreatedFrom() != null && !filters.getCreatedFrom().trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(filters.getCreatedFrom() + "T00:00:00");
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
                    predicateCount++;
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_CREATED_FROM_FILTER",
                            "createdFrom", fromDate.toString());
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_INVALID_CREATED_FROM_FILTER",
                            "invalidCreatedFrom", filters.getCreatedFrom());
                }
            }

            if (filters.getCreatedTo() != null && !filters.getCreatedTo().trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(filters.getCreatedTo() + "T23:59:59");
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
                    predicateCount++;
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_CREATED_TO_FILTER",
                            "createdTo", toDate.toString());
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_INVALID_CREATED_TO_FILTER",
                            "invalidCreatedTo", filters.getCreatedTo());
                }
            }

            // Filter by last maintenance date range
            if (filters.getLastMaintenanceFrom() != null && !filters.getLastMaintenanceFrom().trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(filters.getLastMaintenanceFrom() + "T00:00:00");
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastMaintenance"), fromDate));
                    predicateCount++;
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_MAINTENANCE_FROM_FILTER",
                            "lastMaintenanceFrom", fromDate.toString());
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_INVALID_MAINTENANCE_FROM_FILTER",
                            "invalidLastMaintenanceFrom", filters.getLastMaintenanceFrom());
                }
            }

            if (filters.getLastMaintenanceTo() != null && !filters.getLastMaintenanceTo().trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(filters.getLastMaintenanceTo() + "T23:59:59");
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastMaintenance"), toDate));
                    predicateCount++;
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_ADDED_MAINTENANCE_TO_FILTER",
                            "lastMaintenanceTo", toDate.toString());
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                    LoggingUtil.logBusinessEvent("TERMINAL_SPEC_INVALID_MAINTENANCE_TO_FILTER",
                            "invalidLastMaintenanceTo", filters.getLastMaintenanceTo());
                }
            }

            LoggingUtil.logBusinessEvent("TERMINAL_SPEC_BUILD_COMPLETE",
                    "totalPredicates", String.valueOf(predicateCount),
                    "hasFilters", String.valueOf(predicateCount > 0));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static String buildAppliedFiltersDescription(GetTerminalEDCRequestDTO filters) {
        List<String> appliedFilters = new ArrayList<>();

        if (filters.getStatus() != null && !filters.getStatus().trim().isEmpty()) {
            appliedFilters.add("status=" + filters.getStatus());
        }
        if (filters.getLocation() != null && !filters.getLocation().trim().isEmpty()) {
            appliedFilters.add("location=" + filters.getLocation());
        }
        if (filters.getManufacturer() != null && !filters.getManufacturer().trim().isEmpty()) {
            appliedFilters.add("manufacturer=" + filters.getManufacturer());
        }
        if (filters.getModel() != null && !filters.getModel().trim().isEmpty()) {
            appliedFilters.add("model=" + filters.getModel());
        }
        if (filters.getTerminalType() != null && !filters.getTerminalType().trim().isEmpty()) {
            appliedFilters.add("terminalType=" + filters.getTerminalType());
        }
        if (filters.getIpAddress() != null && !filters.getIpAddress().trim().isEmpty()) {
            appliedFilters.add("ipAddress=" + filters.getIpAddress());
        }
        if (filters.getSerialNumber() != null && !filters.getSerialNumber().trim().isEmpty()) {
            appliedFilters.add("serialNumber=" + filters.getSerialNumber());
        }
        if (filters.getCreatedFrom() != null && !filters.getCreatedFrom().trim().isEmpty()) {
            appliedFilters.add("createdFrom=" + filters.getCreatedFrom());
        }
        if (filters.getCreatedTo() != null && !filters.getCreatedTo().trim().isEmpty()) {
            appliedFilters.add("createdTo=" + filters.getCreatedTo());
        }
        if (filters.getLastMaintenanceFrom() != null && !filters.getLastMaintenanceFrom().trim().isEmpty()) {
            appliedFilters.add("lastMaintenanceFrom=" + filters.getLastMaintenanceFrom());
        }
        if (filters.getLastMaintenanceTo() != null && !filters.getLastMaintenanceTo().trim().isEmpty()) {
            appliedFilters.add("lastMaintenanceTo=" + filters.getLastMaintenanceTo());
        }

        return appliedFilters.isEmpty() ? "No filters applied" : String.join(", ", appliedFilters);
    }
}