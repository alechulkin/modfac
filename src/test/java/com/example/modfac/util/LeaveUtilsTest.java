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

    @Test
    void getLeaveDays_sameDayShouldReturn1() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        int days = LeaveUtils.getLeaveDays(date, date);
        assertEquals(1, days);
    }

    @Test
    void getRandomDate_shouldReturnDateWithinExpectedRange() {
        LocalDate baseDate = LocalDate.of(2025, 1, 1);
        LocalDate randomDate = LeaveUtils.getRandomDate(baseDate);

        long daysBetween = ChronoUnit.DAYS.between(baseDate, randomDate);
        assertThat(daysBetween)
                .isGreaterThanOrEqualTo(0)
                .isLessThan(LeaveUtils.LEAVE_PERIOD_DAYS);
    }

    @RepeatedTest(10)
    void getRandomLeaveType_shouldReturnValidEnumConstant() {
        LeaveType result = LeaveUtils.getRandomLeaveType();
        assertThat(EnumSet.allOf(LeaveType.class)).contains(result);
    }

    @RepeatedTest(10)
    void getRandomStatus_shouldReturnValidEnumConstant() {
        Status result = LeaveUtils.getRandomStatus();
        assertThat(EnumSet.allOf(Status.class)).contains(result);
    }

    @Test
    void leavePeriodStartingDate_shouldBeJanuary1st2025() {
        assertEquals(LocalDate.of(2025, 1, 1), LeaveUtils.LEAVE_PERIOD_STARTING_DATE);
    }
}

