package com.example.modfac.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    /**
     * Exception thrown when an attempt is made to create a username that already exists.
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
