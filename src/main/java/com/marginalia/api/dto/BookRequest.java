package com.marginalia.api.dto;

import com.marginalia.api.domain.BookTopic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Accepts data required to create a book.
 *
 * @param title book title
 * @param author book author
 * @param topic book topic category
 */
public record BookRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @NotNull BookTopic topic
) {
}
