package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that content-block data is incompatible with the selected block type. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidContentBlockException extends RuntimeException {

    /**
     * Creates the exception with a specific type validation message.
     *
     * @param message content-block validation detail
     */
    public InvalidContentBlockException(String message) {
        super(message);
    }
}
