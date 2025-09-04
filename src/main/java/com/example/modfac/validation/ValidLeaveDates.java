package com.example.modfac.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LeaveDatesValidator.class)
@Documented
public @interface ValidLeaveDates {
    String message() default "End date must not be before start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
