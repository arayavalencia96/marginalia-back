package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that an email verification code is unknown, expired, or already used. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVerificationCodeException extends RuntimeException {

    /** Creates the exception with the public verification-code message. */
    public InvalidVerificationCodeException() {
        super("Invalid or expired verification code");
    }
}
