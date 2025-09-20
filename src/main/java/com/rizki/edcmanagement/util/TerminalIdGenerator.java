package com.rizki.edcmanagement.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Pattern: {TYPE}-{LOCATION}-{SEQUENCE}
 * Examples: EDC-JKT-001, ATM-BDG-045, POS-SBY-123
 * Location code must be exactly 3 uppercase letters (e.g., JKT, BDG, SBY)
 */
public class TerminalIdGenerator {
    public enum TerminalType {
        EDC, ATM, POS, KIOSK
    }

    /**
     * Generate terminal ID with specified type, location, and sequence
     * 
     * @param type         Terminal type (EDC, ATM, POS, KIOSK)
     * @param locationCode 3-letter location code (e.g., JKT, BDG, SBY)
     * @param sequence     Sequence number (1-999)
     * @return Formatted terminal ID
     */
    public static String generate(TerminalType type, String locationCode, int sequence) {
        if (!isValidLocationCode(locationCode)) {
            throw new IllegalArgumentException("Location code must be exactly 3 uppercase letters");
        }

        if (sequence < 1 || sequence > 999) {
            throw new IllegalArgumentException("Sequence must be between 1 and 999");
        }

        return String.format("%s-%s-%03d", type.name(), locationCode.toUpperCase(), sequence);
    }

    /**
     * Generate terminal ID with random sequence
     * 
     * @param type         Terminal type
     * @param locationCode 3-letter location code
     * @return Formatted terminal ID with random sequence
     */
    public static String generateRandom(TerminalType type, String locationCode) {
        int randomSequence = ThreadLocalRandom.current().nextInt(1, 1000);
        return generate(type, locationCode, randomSequence);
    }

    /**
     * Validate location code format (exactly 3 uppercase letters)
     * 
     * @param locationCode Location code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLocationCode(String locationCode) {
        return locationCode != null && locationCode.matches("^[A-Z]{3}$");
    }

    public static boolean isValidFormat(String terminalId) {
        if (terminalId == null || terminalId.trim().isEmpty()) {
            return false;
        }

        return terminalId.matches("^(EDC|ATM|POS|KIOSK)-[A-Z]{3}-[0-9]{3}$");
    }

    public static TerminalType extractType(String terminalId) {
        if (!isValidFormat(terminalId)) {
            return null;
        }

        String type = terminalId.split("-")[0];
        try {
            return TerminalType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String extractLocation(String terminalId) {
        if (!isValidFormat(terminalId)) {
            return null;
        }

        return terminalId.split("-")[1];
    }

    public static int extractSequence(String terminalId) {
        if (!isValidFormat(terminalId)) {
            return -1;
        }

        try {
            return Integer.parseInt(terminalId.split("-")[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Get the next available sequence number for a given type and location
     * 
     * @param type                Terminal type
     * @param locationCode        3-letter location code
     * @param existingTerminalIds Set of existing terminal IDs
     * @return Next available sequence number
     */
    public static int getNextSequence(TerminalType type, String locationCode,
            java.util.Set<String> existingTerminalIds) {
        if (!isValidLocationCode(locationCode)) {
            throw new IllegalArgumentException("Location code must be exactly 3 uppercase letters");
        }

        String prefix = type.name() + "-" + locationCode.toUpperCase() + "-";

        int maxSequence = 0;
        for (String existingId : existingTerminalIds) {
            if (existingId.startsWith(prefix)) {
                int sequence = extractSequence(existingId);
                if (sequence > maxSequence) {
                    maxSequence = sequence;
                }
            }
        }

        return maxSequence + 1;
    }
}