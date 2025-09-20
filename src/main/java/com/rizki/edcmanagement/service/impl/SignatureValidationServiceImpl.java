package com.rizki.edcmanagement.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rizki.edcmanagement.service.SignatureValidationService;
import com.rizki.edcmanagement.util.Formatter;

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

                if (expectedSignature.equals(signature))
                    return true;
            } catch (Exception e) {
            }
        }

        return false;
    }
}