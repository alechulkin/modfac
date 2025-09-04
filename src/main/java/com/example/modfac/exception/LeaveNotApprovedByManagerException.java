package com.example.modfac.exception;

public class LeaveNotApprovedByManagerException extends RuntimeException {
    public LeaveNotApprovedByManagerException(String message) {
        super(message);
    }
}