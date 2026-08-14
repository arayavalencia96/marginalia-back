package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordAuthenticationUnavailableException extends RuntimeException {

    public PasswordAuthenticationUnavailableException() {
        super("This account does not have a password");
    }
}
