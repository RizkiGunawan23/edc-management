package com.rizki.edcmanagement.model.enums;

public enum TerminalStatus {
    /**
     * Terminal is operational and available for transactions
     */
    ACTIVE("Active"),

    /**
     * Terminal is temporarily disabled or offline
     */
    INACTIVE("Inactive"),

    /**
     * Terminal is under maintenance or repair
     */
    MAINTENANCE("Under Maintenance"),

    /**
     * Terminal is permanently out of service
     */
    OUT_OF_SERVICE("Out of Service");

    private final String description;

    TerminalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static TerminalStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Status value cannot be null or empty");
        }

        try {
            return TerminalStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid terminal status: " + value +
                    ". Valid values are: ACTIVE, INACTIVE, MAINTENANCE, OUT_OF_SERVICE");
        }
    }

    public boolean isOperational() {
        return this == ACTIVE;
    }

    public boolean requiresAttention() {
        return this == MAINTENANCE || this == OUT_OF_SERVICE;
    }
}