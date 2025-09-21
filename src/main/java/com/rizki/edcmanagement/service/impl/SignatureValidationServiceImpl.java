package com.rizki.edcmanagement.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rizki.edcmanagement.service.SignatureValidationService;
import com.rizki.edcmanagement.util.Formatter;
import com.rizki.edcmanagement.util.LoggingUtil;

@Service
public class SignatureValidationServiceImpl implements SignatureValidationService {
    @Value("${application.security.signature.secret-key}")
    private String secretKey;

    @Value("${application.security.hash-algorithm.name}")
    private String hashAlgorithm;

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes)
            result.append(String.format("%02x", b));

        return result.toString();
    }

    public boolean validateSignatureWithTolerance(String signature, String terminalId, LocalDateTime requestDateTime) {
        String correlationId = LoggingUtil.generateCorrelationId();
        LoggingUtil.setMDC(correlationId, "unknown", "SignatureValidationService");

        try {
            LoggingUtil.logBusinessEvent("SIGNATURE_VALIDATION_STARTED",
                    "Starting signature validation for terminal: " + terminalId +
                            ", requestTime: " + requestDateTime +
                            ", signatureLength: " + signature.length());

            long validationStartTime = System.currentTimeMillis();

            // Try with tolerance of ±2 minutes to account for time differences between
            // Postman and server
            for (int toleranceSeconds = -120; toleranceSeconds <= 120; toleranceSeconds += 1) {
                LocalDateTime adjustedTime = requestDateTime.plusSeconds(toleranceSeconds);

                try {
                    String dateTimeStr = adjustedTime.format(Formatter.DATETIME_FORMATTER);
                    String keyString = dateTimeStr + "|" + secretKey;

                    // Generate signature with this format
                    Mac mac = Mac.getInstance(hashAlgorithm);
                    SecretKeySpec secretKeySpec = new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8),
                            hashAlgorithm);
                    mac.init(secretKeySpec);
                    byte[] signatureBytes = mac.doFinal(terminalId.getBytes(StandardCharsets.UTF_8));
                    String expectedSignature = bytesToHex(signatureBytes);

                    if (expectedSignature.equals(signature)) {
                        long validationTime = System.currentTimeMillis() - validationStartTime;

                        LoggingUtil.logBusinessEvent("SIGNATURE_VALIDATION_SUCCESS",
                                "Signature validation successful for terminal: " + terminalId +
                                        ", toleranceSeconds: " + toleranceSeconds +
                                        ", adjustedTime: " + adjustedTime +
                                        ", validationTime: " + validationTime + "ms" +
                                        ", algorithm: " + hashAlgorithm);

                        LoggingUtil.logPerformance("SIGNATURE_VALIDATION", validationTime);

                        return true;
                    }
                } catch (Exception e) {
                    LoggingUtil.logBusinessEvent("SIGNATURE_VALIDATION_ERROR",
                            "Error during signature validation for terminal: " + terminalId +
                                    ", toleranceSeconds: " + toleranceSeconds +
                                    ", error: " + e.getClass().getSimpleName() +
                                    ", message: " + e.getMessage());
                }
            }

            long validationTime = System.currentTimeMillis() - validationStartTime;

            LoggingUtil.logBusinessEvent("SIGNATURE_VALIDATION_FAILED",
                    "Signature validation failed for terminal: " + terminalId +
                            ", totalAttempts: 241" + // -120 to +120 = 241 attempts
                            ", validationTime: " + validationTime + "ms" +
                            ", reason: No matching signature found within tolerance");

            LoggingUtil.logPerformance("SIGNATURE_VALIDATION_FAILED", validationTime);

            return false;
        } finally {
            LoggingUtil.clearMDC();
        }
    }
}