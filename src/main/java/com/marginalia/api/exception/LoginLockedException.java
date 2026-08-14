package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that login is temporarily locked after too many failed attempts. */
@ResponseStatus(HttpStatus.LOCKED)
public class LoginLockedException extends RuntimeException {

    /** Creates the exception with the public temporary-lock message. */
    public LoginLockedException() {
        super("Login temporarily locked due to too many failed attempts");
    }
}
