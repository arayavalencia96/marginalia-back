package com.marginalia.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Executes queued PDF exports asynchronously and records their final status. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportWorker {

    private final PdfExportJobService pdfExportJobService;
    private final BookPdfGenerator bookPdfGenerator;

    /**
     * Generates a queued PDF export in the configured asynchronous executor.
     *
     * @param exportId identifier of the export job
     * @param bookId identifier of the book to export
     * @param userId identifier of the owning user
     */
    @Async("pdfExportExecutor")
    public void generate(UUID exportId, UUID bookId, UUID userId) {
        try {
            pdfExportJobService.markProcessing(exportId);
            PdfDocumentResult result = bookPdfGenerator.generate(bookId, userId);
            pdfExportJobService.complete(exportId, result);
        } catch (Exception exception) {
            log.error("PDF export job {} failed", exportId, exception);
            try {
                pdfExportJobService.fail(exportId);
            } catch (Exception statusException) {
                log.error("Could not mark PDF export job {} as failed", exportId, statusException);
            }
        }
    }
}
