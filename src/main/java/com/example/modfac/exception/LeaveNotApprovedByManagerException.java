package com.example.modfac.exception;

public class LeaveNotApprovedByManagerException extends RuntimeException {
    /**
     * Constructs a new LeaveNotApprovedByManagerException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public LeaveNotApprovedByManagerException(String message) {
        super(message);
    }

    /**
     * Exception thrown when a leave request is not approved by the manager.
     * <p>
     * This exception is used to indicate that a leave request cannot proceed
     * because it has not been approved by the appropriate authority.
     * </p>
     */
    public LeaveNotApprovedByManagerException(String message) {
        super(message);
    }
}