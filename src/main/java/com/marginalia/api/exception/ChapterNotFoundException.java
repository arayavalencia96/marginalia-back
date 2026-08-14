package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class ChapterNotFoundException extends EntityNotFoundException {

    public ChapterNotFoundException(UUID id) {
        super("Chapter not found: " + id);
    }
}
