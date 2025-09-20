package com.rizki.edcmanagement.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidTerminalStatusValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTerminalStatus {
    String message() default "Invalid status value. Valid values are: ACTIVE, INACTIVE, MAINTENANCE, OUT_OF_SERVICE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}