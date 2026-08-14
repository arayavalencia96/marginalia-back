package com.marginalia.api.exception;

import org.springframework.security.access.AccessDeniedException;

/** Indicates that an authenticated user does not own the requested resource. */
public class ResourceAccessDeniedException extends AccessDeniedException {

    /** Creates the exception with the public ownership-denial message. */
    public ResourceAccessDeniedException() {
        super("You do not have permission to access this resource");
    }
}
