package com.marginalia.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportWorker {

    private final PdfExportJobService pdfExportJobService;
    private final BookPdfGenerator bookPdfGenerator;

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
