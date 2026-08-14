package com.marginalia.api.repository;

import com.marginalia.api.domain.PdfExportJob;
import com.marginalia.api.domain.PdfExportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Provides persistence and owner-scoped queries for asynchronous PDF export jobs. */
public interface PdfExportJobRepository extends JpaRepository<PdfExportJob, UUID> {

    /**
     * Finds an export job by identifier, book, and owner.
     *
     * @param id export job identifier
     * @param bookId book identifier
     * @param userId owner identifier
     * @return matching export job, if present
     */
    Optional<PdfExportJob> findByIdAndBookIdAndUserId(UUID id, UUID bookId, UUID userId);

    /**
     * Finds a lightweight owner-scoped export status projection.
     *
     * @param id export job identifier
     * @param bookId book identifier
     * @param userId owner identifier
     * @return matching status projection, if present
     */
    @Query("select job.id as id, job.bookId as bookId, job.status as status "
            + "from PdfExportJob job "
            + "where job.id = :id and job.bookId = :bookId and job.userId = :userId")
    Optional<PdfExportJobSummary> findStatusByIdAndOwner(
            @Param("id") UUID id,
            @Param("bookId") UUID bookId,
            @Param("userId") UUID userId
    );

    /** Exposes the fields needed to report export status without loading PDF data. */
    interface PdfExportJobSummary {

        /**
         * Returns the export job identifier.
         *
         * @return export job identifier
         */
        UUID getId();

        /**
         * Returns the exported book identifier.
         *
         * @return book identifier
         */
        UUID getBookId();

        /**
         * Returns the current export state.
         *
         * @return export job status
         */
        PdfExportJobStatus getStatus();
    }
}
