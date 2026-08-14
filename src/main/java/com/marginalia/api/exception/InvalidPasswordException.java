package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a password exceeds BCrypt's supported UTF-8 input length. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordException extends RuntimeException {

    /** Creates the exception with the public password-policy message. */
    public InvalidPasswordException() {
        super("Password must not exceed 72 UTF-8 bytes");
    }
}
