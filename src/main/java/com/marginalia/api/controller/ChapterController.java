package com.marginalia.api.controller;

import com.marginalia.api.dto.ChapterRequest;
import com.marginalia.api.dto.ChapterResponse;
import com.marginalia.api.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChapterController {

    private static final String DEBUG_USER_HEADER = "X-Debug-User-Id";

    private final ChapterService chapterService;

    @PostMapping("/books/{bookId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public ChapterResponse create(
            @PathVariable UUID bookId,
            @RequestHeader(DEBUG_USER_HEADER) UUID userId,
            @Valid @RequestBody ChapterRequest request
    ) {
        return chapterService.create(bookId, request, userId);
    }

    @GetMapping("/books/{bookId}/chapters")
    public List<ChapterResponse> findAll(
            @PathVariable UUID bookId,
            @RequestHeader(DEBUG_USER_HEADER) UUID userId
    ) {
        return chapterService.findAll(bookId, userId);
    }

    @PutMapping("/chapters/{id}")
    public ChapterResponse update(
            @PathVariable UUID id,
            @RequestHeader(DEBUG_USER_HEADER) UUID userId,
            @Valid @RequestBody ChapterRequest request
    ) {
        return chapterService.update(id, request, userId);
    }

    @DeleteMapping("/chapters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @RequestHeader(DEBUG_USER_HEADER) UUID userId
    ) {
        chapterService.delete(id, userId);
    }
}
