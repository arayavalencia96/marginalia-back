package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAttachmentException extends RuntimeException {

    public InvalidAttachmentException(String message) {
        super(message);
    }
}
