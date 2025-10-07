package com.example.modfac.util;

import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Status;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LeaveUtilsTest {
    /**
     * Unit tests for the {@link LeaveUtils} utility class.
     *
     * This class contains test cases to verify the correctness of methods in the LeaveUtils class,
     * including calculations for leave days, random date generation, and random enum value selection.
     *
     * The tests ensure that the LeaveUtils methods behave as expected under various scenarios
     * and edge cases, providing confidence in their reliability and accuracy.
     */

    /**
         * Tests the {@link LeaveUtils#getLeaveDays(LocalDate, LocalDate)} method to ensure it
         * correctly calculates the number of leave days between two dates, inclusive of both
         * the start and end dates.
         *
         * This test verifies that the method returns the expected number of days when given
         * a start date of January 1, 2025, and an end date of January 5, 2025. The expected
         * result is 5 days, as the calculation includes both January 1 and January 5.
         */
        @Test
        void getLeaveDays_shouldReturnCorrectNumberOfDaysInclusive() {
            // Given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 5);
    
            // When
            int days = LeaveUtils.getLeaveDays(start, end);
    
            // Then
            assertEquals(5, days); // Inclusive: Jan 1 to Jan 5
        }

    /**
         * Tests the {@link LeaveUtils#getLeaveDays(LocalDate, LocalDate)} method to ensure it
         * correctly calculates the number of leave days when the start and end dates are the same.
         *
         * This test verifies that the method returns 1 day when given the same start and end date
         * of March 15, 2025. The expected result is 1 day, as the calculation includes the single day.
         */
        @Test
        void getLeaveDays_sameDayShouldReturn1() {
            LocalDate date = LocalDate.of(2025, 3, 15);
            int days = LeaveUtils.getLeaveDays(date, date);
            assertEquals(1, days);
        }

    /**
         * Tests the {@link LeaveUtils#getRandomDate(LocalDate)} method to ensure it
         * returns a random date within the expected range.
         *
         * This test verifies that the generated random date is not earlier than the
         * provided base date and is within the range defined by the constant
         * {@link LeaveUtils#LEAVE_PERIOD_DAYS}.
         */
        @Test
        void getRandomDate_shouldReturnDateWithinExpectedRange() {
            LocalDate baseDate = LocalDate.of(2025, 1, 1);
            LocalDate randomDate = LeaveUtils.getRandomDate(baseDate);
    
            long daysBetween = ChronoUnit.DAYS.between(baseDate, randomDate);
            assertThat(daysBetween)
                    .isGreaterThanOrEqualTo(0)
                    .isLessThan(LeaveUtils.LEAVE_PERIOD_DAYS);
        }

    /**
         * Tests the {@link LeaveUtils#getRandomLeaveType()} method to ensure it
         * returns a valid enum constant of the {@link LeaveType} enumeration.
         *
         * This test is repeated multiple times to verify that the method consistently
         * produces valid results across different invocations. It checks that the
         * returned value is one of the constants defined in the {@link LeaveType} enum.
         */
        @RepeatedTest(10)
        void getRandomLeaveType_shouldReturnValidEnumConstant() {
            LeaveType result = LeaveUtils.getRandomLeaveType();
            assertThat(EnumSet.allOf(LeaveType.class)).contains(result);
        }

    /**
         * Tests the {@link LeaveUtils#getRandomStatus()} method to ensure it
         * returns a valid enum constant of the {@link Status} enumeration.
         *
         * This test is repeated multiple times to verify that the method consistently
         * produces valid results across different invocations. It checks that the
         * returned value is one of the constants defined in the {@link Status} enum.
         */
        @RepeatedTest(10)
        void getRandomStatus_shouldReturnValidEnumConstant() {
            Status result = LeaveUtils.getRandomStatus();
            assertThat(EnumSet.allOf(Status.class)).contains(result);
        }

    /**
         * Tests the {@link LeaveUtils#LEAVE_PERIOD_STARTING_DATE} constant to ensure it
         * is correctly set to January 1, 2025.
         *
         * This test verifies that the constant value matches the expected date,
         * providing confidence that the leave period starting date is accurate.
         */
        @Test
        void leavePeriodStartingDate_shouldBeJanuary1st2025() {
            assertEquals(LocalDate.of(2025, 1, 1), LeaveUtils.LEAVE_PERIOD_STARTING_DATE);
        }
}

