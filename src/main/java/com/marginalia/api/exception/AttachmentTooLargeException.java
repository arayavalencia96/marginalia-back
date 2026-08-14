package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that an uploaded attachment exceeds the five-megabyte image limit. */
@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class AttachmentTooLargeException extends RuntimeException {

    /** Creates the exception with the public size-limit message. */
    public AttachmentTooLargeException() {
        super("Image must not exceed 5 MB");
    }
}
