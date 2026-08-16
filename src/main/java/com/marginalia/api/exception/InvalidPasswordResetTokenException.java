package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Signals that a password reset token is invalid, expired, or already consumed. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidPasswordResetTokenException extends RuntimeException {

    /** Creates the exception with a safe client-facing message. */
    public InvalidPasswordResetTokenException() {
        super("The password reset link is invalid or has expired");
    }
}
