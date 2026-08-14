package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

/** Indicates that an asynchronous PDF export job was not found for the requested owner and book. */
public class PdfExportJobNotFoundException extends EntityNotFoundException {

    /** Creates the exception with the public missing-export message. */
    public PdfExportJobNotFoundException() {
        super("PDF export job not found");
    }
}
