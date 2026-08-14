package com.marginalia.api.dto;

import com.marginalia.api.domain.PdfExportJobStatus;

import java.util.UUID;

/**
 * Reports the state and download availability of an asynchronous PDF export.
 *
 * @param exportId export job identifier
 * @param status current export status
 * @param ready whether the PDF can be downloaded
 * @param message human-readable status description
 * @param downloadUrl download path when the export is ready
 */
public record PdfExportStatusResponse(
        UUID exportId,
        PdfExportJobStatus status,
        boolean ready,
        String message,
        String downloadUrl
) {
}
