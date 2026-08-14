package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class ContentBlockNotFoundException extends EntityNotFoundException {

    public ContentBlockNotFoundException(UUID id) {
        super("Content block not found: " + id);
    }
}
