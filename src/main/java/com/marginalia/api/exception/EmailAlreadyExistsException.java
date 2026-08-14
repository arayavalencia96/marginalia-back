package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that an email cannot be used because it is already registered. */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {

    /** Creates the exception with the public email-conflict message. */
    public EmailAlreadyExistsException() {
        super("Email is already registered");
    }
}
