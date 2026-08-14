package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a password-confirmed operation cannot be used by an OAuth-only account. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordAuthenticationUnavailableException extends RuntimeException {

    /** Creates the exception with the public missing-password message. */
    public PasswordAuthenticationUnavailableException() {
        super("This account does not have a password");
    }
}
