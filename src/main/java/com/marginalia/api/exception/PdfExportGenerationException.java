package com.marginalia.api.exception;

/** Indicates that a book could not be rendered as a PDF document. */
public class PdfExportGenerationException extends RuntimeException {

    /**
     * Creates the exception for an underlying rendering failure.
     *
     * @param cause original PDF generation failure
     */
    public PdfExportGenerationException(Throwable cause) {
        super("PDF export could not be generated", cause);
    }
}
