package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a transactional verification email could not be delivered. */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class EmailDeliveryException extends RuntimeException {

    /**
     * Creates the exception for an underlying email-provider failure.
     *
     * @param cause original delivery failure
     */
    public EmailDeliveryException(Throwable cause) {
        super("Verification email could not be sent", cause);
    }
}
