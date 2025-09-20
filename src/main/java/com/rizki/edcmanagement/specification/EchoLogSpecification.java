package com.rizki.edcmanagement.specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.rizki.edcmanagement.dto.echo.request.GetEchoLogRequestDTO;
import com.rizki.edcmanagement.model.EchoLog;
import com.rizki.edcmanagement.model.TerminalEDC;

public class EchoLogSpecification {
    public static Specification<EchoLog> buildSpecification(GetEchoLogRequestDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with TerminalEDC for terminal-related filters
            Join<EchoLog, TerminalEDC> terminalJoin = root.join("terminal");

            // Filter by terminal ID (like search, case insensitive)
            if (filters.getTerminalId() != null && !filters.getTerminalId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(terminalJoin.get("terminalId")),
                        "%" + filters.getTerminalId().toLowerCase().trim() + "%"));
            }

            // Filter by timestamp range
            if (filters.getTimestampFrom() != null && !filters.getTimestampFrom().trim().isEmpty()) {
                try {
                    LocalDate fromDate = LocalDate.parse(filters.getTimestampFrom());
                    Instant fromInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), fromInstant));
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                }
            }

            if (filters.getTimestampTo() != null && !filters.getTimestampTo().trim().isEmpty()) {
                try {
                    LocalDate toDate = LocalDate.parse(filters.getTimestampTo());
                    Instant toInstant = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.lessThan(root.get("timestamp"), toInstant));
                } catch (Exception e) {
                    // Invalid date format, ignore this filter
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static String buildAppliedFiltersDescription(GetEchoLogRequestDTO filters) {
        List<String> appliedFilters = new ArrayList<>();

        if (filters.getTerminalId() != null && !filters.getTerminalId().trim().isEmpty()) {
            appliedFilters.add("terminalId=" + filters.getTerminalId());
        }
        if (filters.getTimestampFrom() != null && !filters.getTimestampFrom().trim().isEmpty()) {
            appliedFilters.add("timestampFrom=" + filters.getTimestampFrom());
        }
        if (filters.getTimestampTo() != null && !filters.getTimestampTo().trim().isEmpty()) {
            appliedFilters.add("timestampTo=" + filters.getTimestampTo());
        }

        return appliedFilters.isEmpty() ? "No filters applied" : String.join(", ", appliedFilters);
    }
}