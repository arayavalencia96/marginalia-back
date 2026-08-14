package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a requested new password is identical to the current password. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NewPasswordMatchesCurrentException extends RuntimeException {

    /** Creates the exception with the public password-reuse message. */
    public NewPasswordMatchesCurrentException() {
        super("New password must be different from the current password");
    }
}
