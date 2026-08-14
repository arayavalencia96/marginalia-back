package com.marginalia.api.service;

import com.marginalia.api.domain.PdfExportJob;
import com.marginalia.api.domain.PdfExportJobStatus;
import com.marginalia.api.exception.PdfExportJobNotFoundException;
import com.marginalia.api.repository.PdfExportJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportJobServiceTest {

    @Mock
    private PdfExportJobRepository repository;

    @InjectMocks
    private PdfExportJobService service;

    @Test
    void createsQueuedJob() {
        when(repository.save(any(PdfExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PdfExportJob job = service.create(UUID.randomUUID(), UUID.randomUUID());

        assertThat(job.getStatus()).isEqualTo(PdfExportJobStatus.QUEUED);
    }

    @Test
    void retrievesOwnedJobAndRejectsMissingJob() {
        UUID id = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PdfExportJob job = PdfExportJob.builder().id(id).bookId(bookId).userId(userId).build();
        when(repository.findByIdAndBookIdAndUserId(id, bookId, userId)).thenReturn(Optional.of(job));

        assertThat(service.requireOwned(id, bookId, userId)).isSameAs(job);

        when(repository.findByIdAndBookIdAndUserId(id, bookId, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.requireOwned(id, bookId, userId))
                .isInstanceOf(PdfExportJobNotFoundException.class);
    }

    @Test
    void transitionsJobThroughProcessingAndCompletion() {
        UUID id = UUID.randomUUID();
        PdfExportJob job = PdfExportJob.builder().id(id).status(PdfExportJobStatus.QUEUED).build();
        when(repository.findById(id)).thenReturn(Optional.of(job));

        service.markProcessing(id);
        assertThat(job.getStatus()).isEqualTo(PdfExportJobStatus.PROCESSING);

        service.complete(id, new PdfDocumentResult(new byte[]{1}, "book.pdf"));
        assertThat(job.getStatus()).isEqualTo(PdfExportJobStatus.COMPLETED);
        assertThat(job.getPdfData()).containsExactly(1);
        assertThat(job.getCompletedAt()).isNotNull();
    }

    @Test
    void marksJobFailedAndRejectsUnknownJob() {
        UUID id = UUID.randomUUID();
        PdfExportJob job = PdfExportJob.builder().id(id).pdfData(new byte[]{1}).build();
        when(repository.findById(id)).thenReturn(Optional.of(job));

        service.fail(id);

        assertThat(job.getStatus()).isEqualTo(PdfExportJobStatus.FAILED);
        assertThat(job.getPdfData()).isNull();
        verify(repository).save(job);

        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.markProcessing(id)).isInstanceOf(PdfExportJobNotFoundException.class);
    }
}
