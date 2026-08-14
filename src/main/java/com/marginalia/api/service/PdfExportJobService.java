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

@Service
@RequiredArgsConstructor
public class PdfExportJobService {

    private final PdfExportJobRepository pdfExportJobRepository;

    @Transactional
    public PdfExportJob create(UUID bookId, UUID userId) {
        PdfExportJob job = PdfExportJob.builder()
                .bookId(bookId)
                .userId(userId)
                .status(PdfExportJobStatus.QUEUED)
                .build();
        return pdfExportJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public PdfExportJob requireOwned(UUID exportId, UUID bookId, UUID userId) {
        return pdfExportJobRepository.findByIdAndBookIdAndUserId(exportId, bookId, userId)
                .orElseThrow(PdfExportJobNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public PdfExportJobSummary requireOwnedStatus(UUID exportId, UUID bookId, UUID userId) {
        return pdfExportJobRepository.findStatusByIdAndOwner(exportId, bookId, userId)
                .orElseThrow(PdfExportJobNotFoundException::new);
    }

    @Transactional
    public void markProcessing(UUID exportId) {
        PdfExportJob job = require(exportId);
        job.setStatus(PdfExportJobStatus.PROCESSING);
        pdfExportJobRepository.save(job);
    }

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
