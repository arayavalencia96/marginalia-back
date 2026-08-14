package com.marginalia.api.domain;

/** Defines the lifecycle states of an asynchronous PDF export job. */
public enum PdfExportJobStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED
}
