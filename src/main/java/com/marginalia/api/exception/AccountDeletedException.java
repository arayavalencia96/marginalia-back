package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that authentication was attempted for a soft-deleted account. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountDeletedException extends RuntimeException {

    /** Creates the exception with the public deleted-account message. */
    public AccountDeletedException() {
        super("This account has been deleted");
    }
}
