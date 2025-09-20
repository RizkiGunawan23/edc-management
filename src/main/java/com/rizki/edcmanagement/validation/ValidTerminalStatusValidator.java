package com.rizki.edcmanagement.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.rizki.edcmanagement.model.enums.TerminalStatus;

public class ValidTerminalStatusValidator implements ConstraintValidator<ValidTerminalStatus, String> {
    @Override
    public void initialize(ValidTerminalStatus constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null or empty values are valid (handled by @NotNull/@NotBlank if required)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Try to parse the string as TerminalStatus enum
        try {
            TerminalStatus.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}