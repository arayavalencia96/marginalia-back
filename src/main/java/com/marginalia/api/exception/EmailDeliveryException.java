package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(Throwable cause) {
        super("Verification email could not be sent", cause);
    }
}
