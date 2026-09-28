package com.marginalia.api.controller;

import com.marginalia.api.dto.ChapterRequest;
import com.marginalia.api.dto.ChapterResponse;
import com.marginalia.api.service.ChapterPdfExportService;
import com.marginalia.api.service.ChapterService;
import com.marginalia.api.service.PdfDocumentResult;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** Exposes authenticated endpoints for managing chapters within books owned by the current user. */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterPdfExportService chapterPdfExportService;

    /**
     * Creates a chapter in an owned book.
     *
     * @param bookId identifier of the target book
     * @param userId identifier of the authenticated user
     * @param request validated chapter data
     * @return the created chapter
     */
    @PostMapping("/books/{bookId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public ChapterResponse create(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChapterRequest request
    ) {
        return chapterService.create(bookId, request, userId);
    }

    /**
     * Lists all chapters in an owned book as a flat ordered collection.
     *
     * @param bookId identifier of the target book
     * @param userId identifier of the authenticated user
     * @return the book's chapters
     */
    @GetMapping("/books/{bookId}/chapters")
    public List<ChapterResponse> findAll(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal UUID userId
    ) {
        return chapterService.findAll(bookId, userId);
    }

    /**
     * Updates an owned chapter.
     *
     * @param id identifier of the chapter to update
     * @param userId identifier of the authenticated user
     * @param request validated replacement chapter data
     * @return the updated chapter
     */
    @PutMapping("/chapters/{id}")
    public ChapterResponse update(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChapterRequest request
    ) {
        return chapterService.update(id, request, userId);
    }

    /**
     * Generates a PDF containing the selected owned chapter and its nested chapters.
     *
     * @param chapterId identifier of the chapter that starts the export
     * @param userId identifier of the authenticated user
     * @return the generated PDF as an attachment response
     */
    @GetMapping("/chapters/{chapterId}/export")
    public ResponseEntity<byte[]> export(
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal UUID userId
    ) {
        PdfDocumentResult document = chapterPdfExportService.export(chapterId, userId);
        String disposition = ContentDisposition.attachment()
                .filename(document.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(document.content().length)
                .body(document.content());
    }

    /**
     * Deletes an owned chapter.
     *
     * @param id identifier of the chapter to delete
     * @param userId identifier of the authenticated user
     */
    @DeleteMapping("/chapters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        chapterService.delete(id, userId);
    }
}
