package com.marginalia.api.service;

public record PdfDocumentResult(
        byte[] content,
        String fileName
) {
}
