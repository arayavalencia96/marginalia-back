package com.marginalia.api.service;

import com.marginalia.api.domain.PdfExportJob;
import com.marginalia.api.domain.PdfExportJobStatus;
import com.marginalia.api.dto.PdfExportStatusResponse;
import com.marginalia.api.exception.PdfExportNotReadyException;
import com.marginalia.api.repository.ContentBlockRepository;
import com.marginalia.api.repository.PdfExportJobRepository.PdfExportJobSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Selects synchronous or asynchronous PDF export and exposes export job status and downloads. */
@Service
@RequiredArgsConstructor
public class BookPdfExportService {

    private static final long ASYNC_BLOCK_THRESHOLD = 50;

    private final ResourceOwnershipService resourceOwnershipService;
    private final ContentBlockRepository contentBlockRepository;
    private final BookPdfGenerator bookPdfGenerator;
    private final PdfExportJobService pdfExportJobService;
    private final PdfExportWorker pdfExportWorker;

    /**
     * Exports a small book immediately or queues a large book for asynchronous generation.
     *
     * @param bookId identifier of the book to export
     * @param userId identifier of the expected owner
     * @return an immediate PDF or queued-job status result
     */
    public ExportResult export(UUID bookId, UUID userId) {
        resourceOwnershipService.requireOwnedBook(bookId, userId);
        long blockCount = contentBlockRepository.countByBookId(bookId);
        if (blockCount <= ASYNC_BLOCK_THRESHOLD) {
            return new ImmediateExport(bookPdfGenerator.generate(bookId, userId));
        }

        PdfExportJob job = pdfExportJobService.create(bookId, userId);
        pdfExportWorker.generate(job.getId(), bookId, userId);
        return new QueuedExport(toStatus(job));
    }

    /**
     * Retrieves the current status of an owned export job.
     *
     * @param bookId identifier of the exported book
     * @param exportId identifier of the export job
     * @param userId identifier of the expected owner
     * @return current export status
     * @throws com.marginalia.api.exception.PdfExportJobNotFoundException if the export job is not owned or does not exist
     */
    public PdfExportStatusResponse status(UUID bookId, UUID exportId, UUID userId) {
        resourceOwnershipService.requireOwnedBook(bookId, userId);
        return toStatus(pdfExportJobService.requireOwnedStatus(exportId, bookId, userId));
    }

    /**
     * Retrieves the generated document for a completed owned export job.
     *
     * @param bookId identifier of the exported book
     * @param exportId identifier of the export job
     * @param userId identifier of the expected owner
     * @return generated PDF bytes and filename
     * @throws com.marginalia.api.exception.PdfExportJobNotFoundException if the export job is not owned or does not exist
     * @throws PdfExportNotReadyException if PDF generation is not complete
     */
    public PdfDocumentResult download(UUID bookId, UUID exportId, UUID userId) {
        resourceOwnershipService.requireOwnedBook(bookId, userId);
        PdfExportJob job = pdfExportJobService.requireOwned(exportId, bookId, userId);
        if (job.getStatus() != PdfExportJobStatus.COMPLETED
                || job.getPdfData() == null
                || job.getFileName() == null) {
            throw new PdfExportNotReadyException();
        }
        return new PdfDocumentResult(job.getPdfData(), job.getFileName());
    }

    private PdfExportStatusResponse toStatus(PdfExportJob job) {
        return toStatus(job.getId(), job.getBookId(), job.getStatus());
    }

    private PdfExportStatusResponse toStatus(PdfExportJobSummary job) {
        return toStatus(job.getId(), job.getBookId(), job.getStatus());
    }

    private PdfExportStatusResponse toStatus(UUID id, UUID bookId, PdfExportJobStatus status) {
        boolean ready = status == PdfExportJobStatus.COMPLETED;
        String downloadUrl = ready
                ? "/api/books/" + bookId + "/exports/" + id + "/download"
                : "";
        String message = switch (status) {
            case QUEUED -> "PDF export queued";
            case PROCESSING -> "PDF export in progress";
            case COMPLETED -> "PDF export ready";
            case FAILED -> "PDF export failed";
        };
        return new PdfExportStatusResponse(id, status, ready, message, downloadUrl);
    }

    /** Represents either an immediately generated PDF or a queued export job. */
    public sealed interface ExportResult permits ImmediateExport, QueuedExport {
    }

    /**
     * Contains a PDF generated synchronously.
     *
     * @param document generated PDF document
     */
    public record ImmediateExport(PdfDocumentResult document) implements ExportResult {
    }

    /**
     * Contains the initial status of an asynchronously queued PDF export.
     *
     * @param status queued export status
     */
    public record QueuedExport(PdfExportStatusResponse status) implements ExportResult {
    }
}
