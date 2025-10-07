package com.example.modfac.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message, which provides more information about the exception.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
