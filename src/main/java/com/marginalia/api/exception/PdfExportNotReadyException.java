package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that a PDF export cannot yet be downloaded because generation is incomplete. */
@ResponseStatus(HttpStatus.CONFLICT)
public class PdfExportNotReadyException extends RuntimeException {

    /** Creates the exception with the public export-state message. */
    public PdfExportNotReadyException() {
        super("PDF export is not ready for download");
    }
}
