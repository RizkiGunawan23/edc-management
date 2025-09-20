package com.rizki.edcmanagement.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.rizki.edcmanagement.dto.terminal.request.GetTerminalEDCRequestDTO;
import com.rizki.edcmanagement.model.TerminalEDC;
import com.rizki.edcmanagement.model.enums.TerminalStatus;

public class TerminalEDCSpecification {
    public static Specification<TerminalEDC> buildSpecification(GetTerminalEDCRequestDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by status
            if (filters.getStatus() != null && !filters.getStatus().trim().isEmpty()) {
                try {
                    TerminalStatus status = TerminalStatus.valueOf(filters.getStatus().toUpperCase().trim());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore this filter
                }
            }

            // Filter by location (contains search, case insensitive)
            if (filters.getLocation() != null && !filters.getLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + filters.getLocation().toLowerCase().trim() + "%"));
            }

            // Filter by manufacturer (contains search, case insensitive)
            if (filters.getManufacturer() != null && !filters.getManufacturer().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("manufacturer")),
                        "%" + filters.getManufacturer().toLowerCase().trim() + "%"));
            }

            // Filter by model (contains search, case insensitive)
            if (filters.getModel() != null && !filters.getModel().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("model")),
                        "%" + filters.getModel().toLowerCase().trim() + "%"));
            }

            // Filter by terminal type (extracted from terminalId)
            if (filters.getTerminalType() != null && !filters.getTerminalType().trim().isEmpty()) {
                String terminalType = filters.getTerminalType().toUpperCase().trim();
                predicates.add(criteriaBuilder.like(
                        root.get("terminalId"),
                        terminalType + "-%"));
            }

            // Filter by IP address (exact match)
            if (filters.getIpAddress() != null && !filters.getIpAddress().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("ipAddress"), filters.getIpAddress().trim()));
            }

            // Filter by serial number (contains search, case insensitive)
            if (filters.getSerialNumber() != null && !filters.getSerialNumber().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serialNumber")),
                        "%" + filters.getSerialNumber().toLowerCase().trim() + "%"));
            }

            // Filter by created date range
            if (filters.getCreatedFrom() != null && !filters.getCreatedFrom().trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(filters.getCreatedFrom() + "T00:00:00");
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                }
            }

            if (filters.getCreatedTo() != null && !filters.getCreatedTo().trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(filters.getCreatedTo() + "T23:59:59");
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                }
            }

            // Filter by last maintenance date range
            if (filters.getLastMaintenanceFrom() != null && !filters.getLastMaintenanceFrom().trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(filters.getLastMaintenanceFrom() + "T00:00:00");
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastMaintenance"), fromDate));
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                }
            }

            if (filters.getLastMaintenanceTo() != null && !filters.getLastMaintenanceTo().trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(filters.getLastMaintenanceTo() + "T23:59:59");
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastMaintenance"), toDate));
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                }
            }

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

        return appliedFilters.isEmpty() ? "No filters applied" : String.join(", ", appliedFilters);
    }
}