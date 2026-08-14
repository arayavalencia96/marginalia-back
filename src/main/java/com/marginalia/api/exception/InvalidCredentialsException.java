package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that password-based authentication or confirmation failed. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {

    /** Creates the exception with a non-disclosing authentication message. */
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
