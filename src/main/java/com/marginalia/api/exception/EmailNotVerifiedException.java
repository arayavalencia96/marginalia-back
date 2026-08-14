package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that an operation requires an account with a verified email address. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmailNotVerifiedException extends RuntimeException {

    /** Creates the exception with the public verification-required message. */
    public EmailNotVerifiedException() {
        super("Email address has not been verified");
    }
}
