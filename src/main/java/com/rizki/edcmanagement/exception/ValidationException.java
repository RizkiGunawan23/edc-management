package com.rizki.edcmanagement.exception;

import java.util.Map;
import java.util.List;

public class ValidationException extends RuntimeException {
    private final Map<String, List<String>> fieldErrors;

    public ValidationException(Map<String, List<String>> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = null;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}