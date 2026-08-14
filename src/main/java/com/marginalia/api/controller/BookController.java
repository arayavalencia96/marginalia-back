package com.marginalia.api.controller;

import com.marginalia.api.dto.BookRequest;
import com.marginalia.api.dto.BookResponse;
import com.marginalia.api.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        bookService.delete(id, userId);
    }
}
