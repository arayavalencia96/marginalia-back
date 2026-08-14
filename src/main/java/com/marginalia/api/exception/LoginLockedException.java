package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)
public class LoginLockedException extends RuntimeException {

    public LoginLockedException() {
        super("Login temporarily locked due to too many failed attempts");
    }
}
