package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NewPasswordMatchesCurrentException extends RuntimeException {

    public NewPasswordMatchesCurrentException() {
        super("New password must be different from the current password");
    }
}
