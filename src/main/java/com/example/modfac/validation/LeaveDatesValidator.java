package com.example.modfac.validation;

import com.example.modfac.dto.CaptureLeaveDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LeaveDatesValidator implements ConstraintValidator<ValidLeaveDates, CaptureLeaveDTO> {

    @Override
    public void initialize(ValidLeaveDates validLeaveDates) {
    }

    @Override
    public boolean isValid(CaptureLeaveDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getStartDate() == null || dto.getEndDate() == null) {
            return true; // Let @NotNull handle this
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            context.disableDefaultConstraintViolation();
            ConstraintValidatorContext endDate = context
                    .buildConstraintViolationWithTemplate("End date cannot be before start date")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}