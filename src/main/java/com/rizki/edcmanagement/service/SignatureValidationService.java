package com.rizki.edcmanagement.service;

import java.time.LocalDateTime;

public interface SignatureValidationService {
    public boolean validateSignatureWithTolerance(String signature, String terminalId, LocalDateTime requestDateTime);
}