package com.marginalia.api.service;

import com.marginalia.api.domain.PdfExportJob;
import com.marginalia.api.domain.PdfExportJobStatus;
import com.marginalia.api.exception.PdfExportNotReadyException;
import com.marginalia.api.repository.ContentBlockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookPdfExportServiceTest {

    @Mock
    private ResourceOwnershipService ownershipService;

    @Mock
    private ContentBlockRepository blockRepository;

    @Mock
    private BookPdfGenerator generator;

    @Mock
    private PdfExportJobService jobService;

    @Mock
    private PdfExportWorker worker;

    @InjectMocks
    private BookPdfExportService service;

    @Test
    void exportsSmallBookImmediately() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PdfDocumentResult document = new PdfDocumentResult(new byte[]{1}, "book.pdf");
        when(blockRepository.countByBookId(bookId)).thenReturn(50L);
        when(generator.generate(bookId, userId)).thenReturn(document);

        var result = service.export(bookId, userId);

        assertThat(result).isEqualTo(new BookPdfExportService.ImmediateExport(document));
    }

    @Test
    void queuesLargeBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID exportId = UUID.randomUUID();
        PdfExportJob job = PdfExportJob.builder()
                .id(exportId).bookId(bookId).userId(userId).status(PdfExportJobStatus.QUEUED).build();
        when(blockRepository.countByBookId(bookId)).thenReturn(51L);
        when(jobService.create(bookId, userId)).thenReturn(job);

        var result = service.export(bookId, userId);

        assertThat(result).isInstanceOf(BookPdfExportService.QueuedExport.class);
        verify(worker).generate(exportId, bookId, userId);
    }

    @Test
    void returnsCompletedDownloadAndRejectsPendingJob() {
        UUID bookId = UUID.randomUUID();
        UUID exportId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PdfExportJob completed = PdfExportJob.builder()
                .status(PdfExportJobStatus.COMPLETED).pdfData(new byte[]{1}).fileName("book.pdf").build();
        when(jobService.requireOwned(exportId, bookId, userId)).thenReturn(completed);

        assertThat(service.download(bookId, exportId, userId).fileName()).isEqualTo("book.pdf");

        completed.setStatus(PdfExportJobStatus.PROCESSING);
        assertThatThrownBy(() -> service.download(bookId, exportId, userId))
                .isInstanceOf(PdfExportNotReadyException.class);
    }
}
