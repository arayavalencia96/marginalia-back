package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.BookTopic;
import com.marginalia.api.exception.PdfExportGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookPdfGeneratorTest {

    @Mock
    private BookExportDataLoader loader;

    @Mock
    private CloudinaryImageDownloader imageDownloader;

    @InjectMocks
    private BookPdfGenerator generator;

    @Test
    void generatesPdfForBookWithoutChapters() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).title("Clean Code").author("Robert Martin").topic(BookTopic.PROGRAMMING).build();
        when(loader.load(bookId, userId)).thenReturn(new BookExportData(book, List.of(), Map.of(), Map.of(), Map.of()));

        PdfDocumentResult result = generator.generate(bookId, userId);

        assertThat(result.fileName()).isEqualTo("clean-code.pdf");
        assertThat(result.content()).startsWith("%PDF".getBytes());
    }

    @Test
    void wrapsRenderingFailure() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book invalid = Book.builder().id(bookId).title(null).author("Author").topic(BookTopic.OTHER).build();
        when(loader.load(bookId, userId)).thenReturn(new BookExportData(invalid, List.of(), Map.of(), Map.of(), Map.of()));

        assertThatThrownBy(() -> generator.generate(bookId, userId))
                .isInstanceOf(PdfExportGenerationException.class);
    }
}
