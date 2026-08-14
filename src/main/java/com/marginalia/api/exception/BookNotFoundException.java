package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class BookNotFoundException extends EntityNotFoundException {

    public BookNotFoundException(UUID id) {
        super("Book not found: " + id);
    }
}
