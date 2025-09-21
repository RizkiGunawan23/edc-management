package com.rizki.edcmanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Utility class untuk structured logging dan correlation ID tracking
 */
public class LoggingUtil {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    private static final Logger businessLogger = LoggerFactory.getLogger(LoggingUtil.class);

    // MDC Keys untuk correlation
    public static final String CORRELATION_ID = "correlationId";
    public static final String TERMINAL_ID = "terminalId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_TYPE = "requestType";
    public static final String CLIENT_IP = "clientIp";

    /**
     * Generate unique correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Extract client IP address from request
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp != null ? clientIp.split(",")[0].trim() : "unknown";
    }

    /**
     * Set MDC context with correlation data
     */
    public static void setMDC(String correlationId, String terminalId, String clientIp) {
        MDC.put(CORRELATION_ID, correlationId != null ? correlationId : "unknown");
        MDC.put(TERMINAL_ID, terminalId != null ? terminalId : "");
        MDC.put(CLIENT_IP, clientIp != null ? clientIp : "unknown");
    }

    /**
     * Clear MDC context
     */
    public static void clearMDC() {
        MDC.clear();
    }

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
     * Log business event dengan structured format
     */
    public static void logBusinessEvent(String event, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("EVENT=").append(event);

        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                sb.append(" | ").append(params[i]).append("=").append(params[i + 1]);
            }
        }

        businessLogger.info(sb.toString());
    }

    /**
     * Log error dengan context
     */
    public static void logError(String event, Exception e, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR_EVENT=").append(event).append(" | ERROR=").append(e.getMessage());

        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                sb.append(" | ").append(params[i]).append("=").append(params[i + 1]);
            }
        }

        businessLogger.error(sb.toString(), e);
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
}