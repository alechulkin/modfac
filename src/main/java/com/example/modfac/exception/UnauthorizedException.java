package com.example.modfac.exception;

public class UnauthorizedException extends RuntimeException {
    /**
         * Constructs a new UnauthorizedException with the specified detail message.
         *
         * @param message the detail message, which provides more information about the exception.
         */
        public UnauthorizedException(String message) {
            super(message);
        }

    /**
     * This exception is thrown to indicate that a user is not authorized to perform the requested operation.
     * It extends the {@link RuntimeException} class, allowing it to be used as an unchecked exception.
     *
     * Usage:
     * Throw this exception when an operation requires authorization and the user does not have the necessary permissions.
     * For example:
     * <pre>
     *     if (!user.hasPermission()) {
     *         throw new UnauthorizedException("User is not authorized to access this resource.");
     *     }
     * </pre>
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
