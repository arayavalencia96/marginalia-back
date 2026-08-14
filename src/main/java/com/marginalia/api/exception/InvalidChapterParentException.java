package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a chapter parent is outside the book or would create a hierarchy cycle. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidChapterParentException extends RuntimeException {

    /**
     * Creates the exception with a specific hierarchy validation message.
     *
     * @param message parent validation detail
     */
    public InvalidChapterParentException(String message) {
        super(message);
    }
}
