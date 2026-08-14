package com.marginalia.api.exception;

import jakarta.persistence.EntityNotFoundException;

public class PdfExportJobNotFoundException extends EntityNotFoundException {

    public PdfExportJobNotFoundException() {
        super("PDF export job not found");
    }
}
