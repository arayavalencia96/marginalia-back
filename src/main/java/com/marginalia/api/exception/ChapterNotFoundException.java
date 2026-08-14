package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

/** Indicates that a requested chapter identifier does not exist. */
public class ChapterNotFoundException extends EntityNotFoundException {

    /**
     * Creates an exception identifying the missing chapter.
     *
     * @param id missing chapter identifier
     */
    public ChapterNotFoundException(UUID id) {
        super("Chapter not found: " + id);
    }
}
