package com.foodsec.auth.exception;

/**
 * Base exception class for authentication-related errors.
 *
 * This exception is thrown when authentication operations fail
 * due to invalid credentials, locked accounts, or other auth issues.
 */
public class AuthException extends RuntimeException {

    private final String errorCode;

    public AuthException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Creates an exception for invalid credentials
     */
    public static AuthException invalidCredentials() {
        return new AuthException("Invalid username or password", "INVALID_CREDENTIALS");
    }

    /**
     * Creates an exception for locked accounts
     */
    public static AuthException accountLocked(String message) {
        return new AuthException(message, "ACCOUNT_LOCKED");
    }

    /**
     * Creates an exception for inactive accounts
     */
    public static AuthException accountInactive() {
        return new AuthException("Account is inactive", "ACCOUNT_INACTIVE");
    }

    /**
     * Creates an exception for MFA verification failures
     */
    public static AuthException mfaVerificationFailed() {
        return new AuthException("MFA verification failed", "MFA_VERIFICATION_FAILED");
    }

    /**
     * Creates an exception for duplicate username
     */
    public static AuthException usernameExists() {
        return new AuthException("Username already exists", "USERNAME_EXISTS");
    }

    /**
     * Creates an exception for duplicate email
     */
    public static AuthException emailExists() {
        return new AuthException("Email already exists", "EMAIL_EXISTS");
    }

    /**
     * Creates an exception for invalid token
     */
    public static AuthException invalidToken() {
        return new AuthException("Invalid or expired token", "INVALID_TOKEN");
    }
}
