package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidContentBlockException extends RuntimeException {

    public InvalidContentBlockException(String message) {
        super(message);
    }
}
