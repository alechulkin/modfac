package com.example.modfac.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new UsernameAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Exception thrown when an attempt is made to create a username that already exists.
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
