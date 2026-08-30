package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class AttachmentNotFoundException extends EntityNotFoundException {

    public AttachmentNotFoundException(UUID id) {
        super("Attachment not found: " + id);
    }
}
