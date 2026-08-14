package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceAccessDeniedException extends RuntimeException {

    public ResourceAccessDeniedException() {
        super("You do not have permission to access this resource");
    }
}
