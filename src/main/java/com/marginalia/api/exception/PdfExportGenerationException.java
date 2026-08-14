package com.marginalia.api.exception;

public class PdfExportGenerationException extends RuntimeException {

    public PdfExportGenerationException(Throwable cause) {
        super("PDF export could not be generated", cause);
    }
}
