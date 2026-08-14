package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PdfExportNotReadyException extends RuntimeException {

    public PdfExportNotReadyException() {
        super("PDF export is not ready for download");
    }
}
