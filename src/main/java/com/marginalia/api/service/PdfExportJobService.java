package com.marginalia.api.service;

import com.marginalia.api.domain.PdfExportJob;
import com.marginalia.api.domain.PdfExportJobStatus;
import com.marginalia.api.exception.PdfExportJobNotFoundException;
import com.marginalia.api.repository.PdfExportJobRepository;
import com.marginalia.api.repository.PdfExportJobRepository.PdfExportJobSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Persists PDF export jobs and controls their state transitions and generated document data. */
@Service
@RequiredArgsConstructor
public class PdfExportJobService {

    private final PdfExportJobRepository pdfExportJobRepository;

    /**
     * Creates a queued export job for an owned book.
     *
     * @param bookId identifier of the book to export
     * @param userId identifier of the user who requested the export
     * @return the persisted queued job
     */
    @Transactional
    public PdfExportJob create(UUID bookId, UUID userId) {
        PdfExportJob job = PdfExportJob.builder()
                .bookId(bookId)
                .userId(userId)
                .status(PdfExportJobStatus.QUEUED)
                .build();
        return pdfExportJobRepository.save(job);
    }

    /**
     * Retrieves an export job matching its book and owner.
     *
     * @param exportId identifier of the export job
     * @param bookId identifier of the exported book
     * @param userId identifier of the expected owner
     * @return the owned export job
     * @throws PdfExportJobNotFoundException if no matching job exists
     */
    @Transactional(readOnly = true)
    public PdfExportJob requireOwned(UUID exportId, UUID bookId, UUID userId) {
        return pdfExportJobRepository.findByIdAndBookIdAndUserId(exportId, bookId, userId)
                .orElseThrow(PdfExportJobNotFoundException::new);
    }

    /**
     * Retrieves a lightweight status projection for an owned export job.
     *
     * @param exportId identifier of the export job
     * @param bookId identifier of the exported book
     * @param userId identifier of the expected owner
     * @return status projection for the owned job
     * @throws PdfExportJobNotFoundException if no matching job exists
     */
    @Transactional(readOnly = true)
    public PdfExportJobSummary requireOwnedStatus(UUID exportId, UUID bookId, UUID userId) {
        return pdfExportJobRepository.findStatusByIdAndOwner(exportId, bookId, userId)
                .orElseThrow(PdfExportJobNotFoundException::new);
    }

    /**
     * Marks an export job as processing.
     *
     * @param exportId identifier of the export job
     * @throws PdfExportJobNotFoundException if the job does not exist
     */
    @Transactional
    public void markProcessing(UUID exportId) {
        PdfExportJob job = require(exportId);
        job.setStatus(PdfExportJobStatus.PROCESSING);
        pdfExportJobRepository.save(job);
    }

    /**
     * Completes an export job and stores the generated PDF.
     *
     * @param exportId identifier of the export job
     * @param result generated PDF bytes and filename
     * @throws PdfExportJobNotFoundException if the job does not exist
     */
    @Transactional
    public void complete(UUID exportId, PdfDocumentResult result) {
        PdfExportJob job = require(exportId);
        job.setStatus(PdfExportJobStatus.COMPLETED);
        job.setFileName(result.fileName());
        job.setPdfData(result.content());
        job.setErrorMessage(null);
        job.setCompletedAt(Instant.now());
        pdfExportJobRepository.save(job);
    }

    /**
     * Marks an export job as failed and removes any generated document data.
     *
     * @param exportId identifier of the export job
     * @throws PdfExportJobNotFoundException if the job does not exist
     */
    @Transactional
    public void fail(UUID exportId) {
        PdfExportJob job = require(exportId);
        job.setStatus(PdfExportJobStatus.FAILED);
        job.setPdfData(null);
        job.setErrorMessage("PDF export could not be generated");
        job.setCompletedAt(Instant.now());
        pdfExportJobRepository.save(job);
    }

    private PdfExportJob require(UUID exportId) {
        return pdfExportJobRepository.findById(exportId)
                .orElseThrow(PdfExportJobNotFoundException::new);
    }
}
