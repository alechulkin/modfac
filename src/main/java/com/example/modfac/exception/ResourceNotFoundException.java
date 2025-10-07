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

    /**
     * Exception thrown when a requested resource is not found.
     * <p>
     * This exception is a runtime exception and can be used to indicate
     * that a specific resource, such as an entity or file, could not be located.
     * </p>
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
