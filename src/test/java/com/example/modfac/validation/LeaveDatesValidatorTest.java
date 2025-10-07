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
    /**
     * Unit tests for the {@link LeaveDatesValidator} class.
     *
     * This test class validates the behavior of the LeaveDatesValidator, ensuring that it correctly
     * validates leave date ranges based on the business rules. The tests cover scenarios such as:
     * - Valid date ranges where the end date is after the start date.
     * - Invalid date ranges where the end date is before the start date.
     * - Null DTOs or null start/end dates.
     *
     * Mocking is used for the {@link ConstraintValidatorContext} to simulate the validation context
     * and verify interactions with it.
     */

    private LeaveDatesValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

        /**
         * Initializes the test environment by setting up mocks and creating instances of the required objects.
         *
         * This method is executed before each test case to ensure a clean and isolated test environment.
         * It sets up the {@link LeaveDatesValidator} instance and mocks for the {@link ConstraintValidatorContext}
         * and its nested builders to simulate the validation context and interactions.
         */
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

        /**
         * Tests the {@link LeaveDatesValidator#isValid} method to ensure it returns true
         * when the provided dates are valid.
         *
         * This test case verifies that the validator correctly identifies a valid date range
         * where the end date is after the start date. It also ensures that no constraint
         * violations are added to the {@link ConstraintValidatorContext}.
         */
        @Test
        void isValid_shouldReturnTrue_whenDatesAreValid() {
            CaptureLeaveDTO dto = new CaptureLeaveDTO();
            dto.setStartDate(LocalDate.of(2025, 9, 10));
            dto.setEndDate(LocalDate.of(2025, 9, 12));
    
            boolean result = validator.isValid(dto, context);
    
            assertTrue(result);
            verify(context, never()).buildConstraintViolationWithTemplate(anyString());
        }

        /**
         * Tests the {@link LeaveDatesValidator#isValid} method to ensure it returns false
         * when the end date is before the start date.
         *
         * This test case verifies that the validator correctly identifies an invalid date range
         * where the end date precedes the start date. It also ensures that appropriate constraint
         * violations are added to the {@link ConstraintValidatorContext}.
         */
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

        /**
         * Tests the {@link LeaveDatesValidator#isValid} method to ensure it returns true
         * when the provided DTO is null.
         *
         * This test case verifies that the validator correctly handles a null DTO by
         * returning true and not interacting with the {@link ConstraintValidatorContext}.
         */
        @Test
        void isValid_shouldReturnTrue_whenDtoIsNull() {
            boolean result = validator.isValid(null, context);
    
            assertTrue(result);
            verifyNoInteractions(context);
        }

        /**
         * Tests the {@link LeaveDatesValidator#isValid} method to ensure it returns true
         * when either the start date or the end date is null.
         *
         * This test case verifies that the validator correctly handles scenarios where
         * one of the dates is null by returning true and not interacting with the
         * {@link ConstraintValidatorContext}.
         */
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

