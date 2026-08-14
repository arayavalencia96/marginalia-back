package com.marginalia.api.dto;

import com.marginalia.api.domain.PdfExportJobStatus;

import java.util.UUID;

public record PdfExportStatusResponse(
        UUID exportId,
        PdfExportJobStatus status,
        boolean ready,
        String message,
        String downloadUrl
) {
}
