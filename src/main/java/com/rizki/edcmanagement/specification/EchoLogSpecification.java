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
import com.rizki.edcmanagement.util.LoggingUtil;

public class EchoLogSpecification {
    public static Specification<EchoLog> buildSpecification(GetEchoLogRequestDTO filters) {
        return (root, query, criteriaBuilder) -> {
            String correlationId = LoggingUtil.generateCorrelationId();
            LoggingUtil.setMDC(correlationId, "unknown", "EchoLogSpecification");

            try {
                LoggingUtil.logBusinessEvent("SPECIFICATION_BUILD_START",
                        "Building EchoLog specification with filters: " + buildAppliedFiltersDescription(filters));

                List<Predicate> predicates = new ArrayList<>();

                // Join with TerminalEDC for terminal-related filters
                Join<EchoLog, TerminalEDC> terminalJoin = root.join("terminal");

                // Filter by terminal ID (like search, case insensitive)
                if (filters.getTerminalId() != null && !filters.getTerminalId().trim().isEmpty()) {
                    String terminalIdFilter = "%" + filters.getTerminalId().toLowerCase().trim() + "%";
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(terminalJoin.get("terminalId")),
                            terminalIdFilter));
                    LoggingUtil.logBusinessEvent("SPECIFICATION_TERMINAL_FILTER",
                            "Added terminal ID filter: " + terminalIdFilter);
                }

                // Filter by timestamp range
                if (filters.getTimestampFrom() != null && !filters.getTimestampFrom().trim().isEmpty()) {
                    try {
                        LocalDate fromDate = LocalDate.parse(filters.getTimestampFrom());
                        Instant fromInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), fromInstant));
                        LoggingUtil.logBusinessEvent("SPECIFICATION_TIMESTAMP_FROM_FILTER",
                                "Added timestamp from filter: " + fromInstant);
                    } catch (Exception e) {
                        LoggingUtil.logBusinessEvent("SPECIFICATION_TIMESTAMP_FROM_ERROR",
                                "Invalid timestamp from format: " + filters.getTimestampFrom() + ", error: "
                                        + e.getMessage());
                    }
                }

                if (filters.getTimestampTo() != null && !filters.getTimestampTo().trim().isEmpty()) {
                    try {
                        LocalDate toDate = LocalDate.parse(filters.getTimestampTo());
                        Instant toInstant = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                        predicates.add(criteriaBuilder.lessThan(root.get("timestamp"), toInstant));
                        LoggingUtil.logBusinessEvent("SPECIFICATION_TIMESTAMP_TO_FILTER",
                                "Added timestamp to filter: " + toInstant);
                    } catch (Exception e) {
                        LoggingUtil.logBusinessEvent("SPECIFICATION_TIMESTAMP_TO_ERROR",
                                "Invalid timestamp to format: " + filters.getTimestampTo() + ", error: "
                                        + e.getMessage());
                    }
                }

                Predicate finalPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                LoggingUtil.logBusinessEvent("SPECIFICATION_BUILD_SUCCESS",
                        "Successfully built specification with " + predicates.size() + " predicates");

                return finalPredicate;
            } finally {
                LoggingUtil.clearMDC();
            }
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