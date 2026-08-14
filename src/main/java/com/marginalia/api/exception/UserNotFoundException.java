package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

/** Indicates that an active user account could not be found. */
public class UserNotFoundException extends EntityNotFoundException {

    /** Creates the exception with the public missing-user message. */
    public UserNotFoundException() {
        super("User not found");
    }
}
