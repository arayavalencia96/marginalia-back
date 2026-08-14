package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

/** Indicates that a requested book identifier does not exist. */
public class BookNotFoundException extends EntityNotFoundException {

    /**
     * Creates an exception identifying the missing book.
     *
     * @param id missing book identifier
     */
    public BookNotFoundException(UUID id) {
        super("Book not found: " + id);
    }
}
