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

}