package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

/** Indicates that a requested content-block identifier does not exist. */
public class ContentBlockNotFoundException extends EntityNotFoundException {

    /**
     * Creates an exception identifying the missing content block.
     *
     * @param id missing content-block identifier
     */
    public ContentBlockNotFoundException(UUID id) {
        super("Content block not found: " + id);
    }
}
