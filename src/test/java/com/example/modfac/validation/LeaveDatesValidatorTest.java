package com.example.modfac.validation;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.validation.LeaveDatesValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveDatesValidatorTest {

    private LeaveDatesValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new LeaveDatesValidator();
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        // Chain mocking for .buildConstraintViolationWithTemplate(...).addPropertyNode(...).addConstraintViolation()
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context); // Not used, but safe for chaining
    }

    @Test
    void isValid_shouldReturnTrue_whenDatesAreValid() {
        CaptureLeaveDTO dto = new CaptureLeaveDTO();
        dto.setStartDate(LocalDate.of(2025, 9, 10));
        dto.setEndDate(LocalDate.of(2025, 9, 12));

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_shouldReturnFalse_whenEndDateIsBeforeStartDate() {
        CaptureLeaveDTO dto = new CaptureLeaveDTO();
        dto.setStartDate(LocalDate.of(2025, 9, 15));
        dto.setEndDate(LocalDate.of(2025, 9, 10));

        boolean result = validator.isValid(dto, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("End date cannot be before start date");
    }

    @Test
    void isValid_shouldReturnTrue_whenDtoIsNull() {
        boolean result = validator.isValid(null, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnTrue_whenStartDateOrEndDateIsNull() {
        CaptureLeaveDTO dto = new CaptureLeaveDTO();
        dto.setStartDate(null);
        dto.setEndDate(LocalDate.of(2025, 9, 10));

        assertTrue(validator.isValid(dto, context));

        dto.setStartDate(LocalDate.of(2025, 9, 10));
        dto.setEndDate(null);

        assertTrue(validator.isValid(dto, context));
    }
}

