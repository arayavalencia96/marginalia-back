package com.marginalia.api.controller;

import com.marginalia.api.dto.BookRequest;
import com.marginalia.api.dto.BookResponse;
import com.marginalia.api.dto.PdfExportStatusResponse;
import com.marginalia.api.service.BookPdfExportService;
import com.marginalia.api.service.BookService;
import com.marginalia.api.service.PdfDocumentResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookPdfExportService bookPdfExportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody BookRequest request
    ) {
        return bookService.create(request, userId);
    }

    @GetMapping
    public List<BookResponse> findAll(@AuthenticationPrincipal UUID userId) {
        return bookService.findAll(userId);
    }

    @GetMapping("/{id}")
    public BookResponse findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        return bookService.findById(id, userId);
    }

    @GetMapping("/{bookId}/export")
    public ResponseEntity<?> export(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal UUID userId
    ) {
        BookPdfExportService.ExportResult result = bookPdfExportService.export(bookId, userId);
        if (result instanceof BookPdfExportService.ImmediateExport immediate) {
            return pdfResponse(immediate.document());
        }

        PdfExportStatusResponse status = ((BookPdfExportService.QueuedExport) result).status();
        URI statusLocation = URI.create("/api/books/" + bookId + "/exports/" + status.exportId());
        return ResponseEntity.accepted().location(statusLocation).body(status);
    }

    @GetMapping("/{bookId}/exports/{exportId}")
    public PdfExportStatusResponse exportStatus(
            @PathVariable UUID bookId,
            @PathVariable UUID exportId,
            @AuthenticationPrincipal UUID userId
    ) {
        return bookPdfExportService.status(bookId, exportId, userId);
    }

    @GetMapping("/{bookId}/exports/{exportId}/download")
    public ResponseEntity<byte[]> downloadExport(
            @PathVariable UUID bookId,
            @PathVariable UUID exportId,
            @AuthenticationPrincipal UUID userId
    ) {
        return pdfResponse(bookPdfExportService.download(bookId, exportId, userId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        bookService.delete(id, userId);
    }

    private ResponseEntity<byte[]> pdfResponse(PdfDocumentResult document) {
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
}
