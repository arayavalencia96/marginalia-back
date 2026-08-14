package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a refresh token is unknown, expired, revoked, or no longer eligible. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidRefreshTokenException extends RuntimeException {

    /** Creates the exception with the public refresh-token message. */
    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
