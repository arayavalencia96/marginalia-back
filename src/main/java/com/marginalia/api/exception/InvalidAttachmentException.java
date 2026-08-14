package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that uploaded attachment data does not satisfy image requirements. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAttachmentException extends RuntimeException {

    /**
     * Creates the exception with a specific validation message.
     *
     * @param message attachment validation detail
     */
    public InvalidAttachmentException(String message) {
        super(message);
    }
}
