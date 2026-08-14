package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class AttachmentTooLargeException extends RuntimeException {

    public AttachmentTooLargeException() {
        super("Image must not exceed 5 MB");
    }
}
