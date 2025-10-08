package com.example.modfac.util;

import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class LeaveUtils {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LeaveUtils.class);
    public static final int LEAVE_PERIOD_DAYS = 365;
    public static final LocalDate LEAVE_PERIOD_STARTING_DATE = LocalDate.of(2025, 1, 1);

    private static final Random RANDOM = ThreadLocalRandom.current();

    private LeaveUtils() {
    }

    public static int getLeaveDays(LocalDate startDate, LocalDate endDate) {
        LOGGER.debug("getLeaveDays method invoked");
        int leaveDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LOGGER.debug("getLeaveDays method finished");
        return leaveDays;
    }

    public static LocalDate getRandomDate(LocalDate startDate) {
        LOGGER.debug("getRandomDate method invoked");
        long randomDays = getRandomDays();
        LocalDate resultDate = startDate.plusDays(randomDays);
        LOGGER.debug("getRandomDate method finished");
        return resultDate;
    }

    public static LeaveType getRandomLeaveType() {
        LOGGER.debug("getRandomLeaveType method invoked");
        LeaveType[] leaveTypes = LeaveType.values();
        LeaveType result = leaveTypes[RANDOM.nextInt(leaveTypes.length)];
        LOGGER.debug("getRandomLeaveType method finished");
        return result;
    }

    public static Status getRandomStatus() {
        LOGGER.debug("getRandomStatus method invoked");
        Status result = Status.values()[RANDOM.nextInt(Status.values().length)];
        LOGGER.debug("getRandomStatus method finished");
        return result;
    }

    private static long getRandomDays() {
        LOGGER.debug("getRandomDays method invoked");
        long randomDays = (long) (RANDOM.nextDouble() * LEAVE_PERIOD_DAYS);
        LOGGER.debug("getRandomDays method finished");
        return randomDays;
    }
}