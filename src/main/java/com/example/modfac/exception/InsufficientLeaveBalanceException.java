package com.example.modfac.exception;

public class InsufficientLeaveBalanceException extends RuntimeException {
    /**
     * Exception thrown when an operation attempts to deduct leave days
     * but the available leave balance is insufficient.
     *
     * This exception extends {@link RuntimeException} and is typically used
     * in scenarios where leave balance validation fails.
     *
     * Example usage:
     * <pre>
     *     if (leaveBalance < requestedLeaveDays) {
     *         throw new InsufficientLeaveBalanceException("Insufficient leave balance.");
     *     }
     * </pre>
     */
    public InsufficientLeaveBalanceException(String message) {
        super(message);
    }
}
