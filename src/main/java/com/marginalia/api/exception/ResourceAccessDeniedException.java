package com.marginalia.api.exception;

import org.springframework.security.access.AccessDeniedException;

public class ResourceAccessDeniedException extends AccessDeniedException {

    public ResourceAccessDeniedException() {
        super("You do not have permission to access this resource");
    }
}
