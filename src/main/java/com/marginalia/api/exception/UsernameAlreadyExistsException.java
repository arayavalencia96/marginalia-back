package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a username cannot be used because it is already registered. */
@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyExistsException extends RuntimeException {

    /** Creates the exception with the public username-conflict message. */
    public UsernameAlreadyExistsException() {
        super("Username is already registered");
    }
}
