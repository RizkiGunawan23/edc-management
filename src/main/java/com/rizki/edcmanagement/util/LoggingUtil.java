package com.rizki.edcmanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Utility class untuk structured logging dan correlation ID tracking
 */
public class LoggingUtil {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

    // MDC Keys untuk correlation
    public static final String CORRELATION_ID = "correlationId";
    public static final String TERMINAL_ID = "terminalId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_TYPE = "requestType";

    /**
     * Set correlation context untuk request tracking
     */
    public static void setCorrelationContext(String correlationId, String terminalId, String requestType) {
        MDC.put(CORRELATION_ID, correlationId);
        MDC.put(TERMINAL_ID, terminalId);
        MDC.put(REQUEST_TYPE, requestType);
    }

    /**
     * Clear correlation context setelah request selesai
     */
    public static void clearCorrelationContext() {
        MDC.clear();
    }

    /**
     * Log audit event dengan context
     */
    public static void logAuditEvent(String event, String details) {
        auditLogger.info("AUDIT_EVENT={} | DETAILS={}", event, details);
    }

    /**
     * Log performance metrics
     */
    public static void logPerformance(String operation, long durationMs) {
        performanceLogger.info("OPERATION={} | DURATION_MS={}", operation, durationMs);
    }

    /**
     * Log business event dengan structured format
     */
    public static void logBusinessEvent(Logger logger, String event, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("EVENT=").append(event);

        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                sb.append(" | ").append(params[i]).append("=").append(params[i + 1]);
            }
        }

        logger.info(sb.toString());
    }
}