package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ContentBlockNotFoundException extends RuntimeException {

    public ContentBlockNotFoundException(UUID id) {
        super("Content block not found: " + id);
    }
}
