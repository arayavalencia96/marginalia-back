package com.marginalia.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportWorkerTest {

    @Mock
    private PdfExportJobService jobService;

    @Mock
    private BookPdfGenerator generator;

    @InjectMocks
    private PdfExportWorker worker;

    @Test
    void completesSuccessfulExport() {
        UUID exportId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PdfDocumentResult result = new PdfDocumentResult(new byte[]{1}, "book.pdf");
        when(generator.generate(bookId, userId)).thenReturn(result);

        worker.generate(exportId, bookId, userId);

        verify(jobService).markProcessing(exportId);
        verify(jobService).complete(exportId, result);
    }

    @Test
    void marksFailedExport() {
        UUID exportId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalStateException("failure")).when(generator).generate(bookId, userId);

        worker.generate(exportId, bookId, userId);

        verify(jobService).fail(exportId);
    }
}
