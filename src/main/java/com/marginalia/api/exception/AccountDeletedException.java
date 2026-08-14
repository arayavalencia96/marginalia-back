package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountDeletedException extends RuntimeException {

    public AccountDeletedException() {
        super("This account has been deleted");
    }
}
