package com.marginalia.api.service;

/**
 * Contains an in-memory PDF and its suggested download filename.
 *
 * @param content generated PDF bytes
 * @param fileName suggested attachment filename
 */
public record PdfDocumentResult(
        byte[] content,
        String fileName
) {
}
