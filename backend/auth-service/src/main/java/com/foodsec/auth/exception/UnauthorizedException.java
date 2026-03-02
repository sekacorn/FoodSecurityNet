package com.foodsec.auth.exception;

/**
 * Exception thrown when a user attempts to access a resource without
 * proper authentication or authorization.
 */
public class UnauthorizedException extends RuntimeException {

    private final String errorCode;

    public UnauthorizedException(String message) {
        super(message);
        this.errorCode = "UNAUTHORIZED";
    }

    public UnauthorizedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNAUTHORIZED";
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Creates an exception for missing authentication
     */
    public static UnauthorizedException notAuthenticated() {
        return new UnauthorizedException("Authentication required", "NOT_AUTHENTICATED");
    }

    /**
     * Creates an exception for insufficient permissions
     */
    public static UnauthorizedException insufficientPermissions() {
        return new UnauthorizedException("Insufficient permissions", "INSUFFICIENT_PERMISSIONS");
    }

    /**
     * Creates an exception for expired session
     */
    public static UnauthorizedException sessionExpired() {
        return new UnauthorizedException("Session has expired", "SESSION_EXPIRED");
    }

    /**
     * Creates an exception for invalid token
     */
    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException("Invalid authentication token", "INVALID_TOKEN");
    }
}
