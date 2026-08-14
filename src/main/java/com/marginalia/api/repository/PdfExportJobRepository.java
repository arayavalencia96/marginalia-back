package com.marginalia.api.repository;

import com.marginalia.api.domain.PdfExportJob;
import com.marginalia.api.domain.PdfExportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PdfExportJobRepository extends JpaRepository<PdfExportJob, UUID> {

    Optional<PdfExportJob> findByIdAndBookIdAndUserId(UUID id, UUID bookId, UUID userId);

    @Query("select job.id as id, job.bookId as bookId, job.status as status "
            + "from PdfExportJob job "
            + "where job.id = :id and job.bookId = :bookId and job.userId = :userId")
    Optional<PdfExportJobSummary> findStatusByIdAndOwner(
            @Param("id") UUID id,
            @Param("bookId") UUID bookId,
            @Param("userId") UUID userId
    );

    interface PdfExportJobSummary {

        UUID getId();

        UUID getBookId();

        PdfExportJobStatus getStatus();
    }
}
