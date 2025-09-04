package com.example.modfac.util;

import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class LeaveUtils {
    public static final int LEAVE_PERIOD_DAYS = 365;
    public static final LocalDate LEAVE_PERIOD_STARTING_DATE = LocalDate.of(2025, 1, 1);

    private static final Random RANDOM = ThreadLocalRandom.current();

    private LeaveUtils() {
    }

    public static int getLeaveDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public static LocalDate getRandomDate(LocalDate startDate) {
        long randomDays = getRandomDays();
        return startDate.plusDays(randomDays);
    }

    public static LeaveType getRandomLeaveType() {
        return LeaveType.values()[RANDOM.nextInt(LeaveType.values().length)];
    }

    public static Status getRandomStatus() {
        return Status.values()[RANDOM.nextInt(Status.values().length)];
    }

    private static long getRandomDays() {
        return (long) (RANDOM.nextDouble() * LEAVE_PERIOD_DAYS);
    }
}
