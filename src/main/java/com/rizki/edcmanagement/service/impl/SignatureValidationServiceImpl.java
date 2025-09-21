package com.rizki.edcmanagement.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rizki.edcmanagement.service.SignatureValidationService;
import com.rizki.edcmanagement.util.Formatter;
import com.rizki.edcmanagement.util.LoggingUtil;

@Service
public class SignatureValidationServiceImpl implements SignatureValidationService {
    private static final Logger logger = LoggerFactory.getLogger(SignatureValidationServiceImpl.class);

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
        LoggingUtil.logBusinessEvent(logger, "SIGNATURE_VALIDATION_STARTED",
                "TERMINAL_ID", terminalId,
                "REQUEST_TIME", requestDateTime,
                "SIGNATURE_LENGTH", signature.length());

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

                    LoggingUtil.logBusinessEvent(logger, "SIGNATURE_VALIDATION_SUCCESS",
                            "TERMINAL_ID", terminalId,
                            "TOLERANCE_SECONDS", toleranceSeconds,
                            "ADJUSTED_TIME", adjustedTime,
                            "VALIDATION_TIME_MS", validationTime,
                            "ALGORITHM", hashAlgorithm);

                    LoggingUtil.logPerformance("SIGNATURE_VALIDATION", validationTime);

                    return true;
                }
            } catch (Exception e) {
                LoggingUtil.logBusinessEvent(logger, "SIGNATURE_VALIDATION_ERROR",
                        "TERMINAL_ID", terminalId,
                        "TOLERANCE_SECONDS", toleranceSeconds,
                        "ERROR", e.getClass().getSimpleName(),
                        "MESSAGE", e.getMessage());
            }
        }

        long validationTime = System.currentTimeMillis() - validationStartTime;

        LoggingUtil.logBusinessEvent(logger, "SIGNATURE_VALIDATION_FAILED",
                "TERMINAL_ID", terminalId,
                "TOTAL_ATTEMPTS", 241, // -120 to +120 = 241 attempts
                "VALIDATION_TIME_MS", validationTime,
                "REASON", "No matching signature found within tolerance");

        LoggingUtil.logPerformance("SIGNATURE_VALIDATION_FAILED", validationTime);

        return false;
    }
}