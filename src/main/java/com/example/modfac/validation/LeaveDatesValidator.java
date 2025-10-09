package com.example.modfac.validation;

import com.example.modfac.dto.CaptureLeaveDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LeaveDatesValidator implements ConstraintValidator<ValidLeaveDates, CaptureLeaveDTO> {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LeaveDatesValidator.class);

    @Override
    public void initialize(ValidLeaveDates validLeaveDates) {
        LOGGER.debug("initialize method invoked");
        LOGGER.debug("initialize method finished");
    }

    @Override
    public boolean isValid(CaptureLeaveDTO dto, ConstraintValidatorContext context) {
        LOGGER.debug("isValid method invoked");
    
        if (dto == null || dto.getStartDate() == null || dto.getEndDate() == null) {
            return true; // Let @NotNull handle this
        }
    
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("End date cannot be before start date")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }
    
        LOGGER.debug("isValid method finished");
        return true;
    }

}