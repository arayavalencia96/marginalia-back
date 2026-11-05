package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.BookTopic;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.domain.HeadingLevel;
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
    void generatesPdfWithResolvedAndUnresolvedExercises() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).title("Study Plan").author("Reader").topic(BookTopic.OTHER).build();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).title("Exercises").build();
        ContentBlock unresolved = ContentBlock.builder()
                .id(UUID.randomUUID())
                .chapterId(chapterId)
                .type(ContentBlockType.EXERCISE)
                .content("Review chapter one")
                .resolved(false)
                .build();
        ContentBlock resolved = ContentBlock.builder()
                .id(UUID.randomUUID())
                .chapterId(chapterId)
                .type(ContentBlockType.EXERCISE)
                .content("Complete practice test")
                .resolved(true)
                .build();
        when(loader.load(bookId, userId)).thenReturn(new BookExportData(
                book,
                List.of(chapter),
                Map.of(chapterId, List.of(unresolved, resolved)),
                Map.of(),
                Map.of()
        ));

        PdfDocumentResult result = generator.generate(bookId, userId);

        assertThat(result.fileName()).isEqualTo("study-plan.pdf");
        assertThat(result.content()).startsWith("%PDF".getBytes());
        assertThat(result.content()).hasSizeGreaterThan(1_000);
    }

    @Test
    void generatesPdfForSelectedChapterAndItsNestedChapters() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID nestedChapterId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).title("Study Notes").author("Reader").topic(BookTopic.OTHER).build();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).title("Arrays").build();
        Chapter nestedChapter = Chapter.builder().id(nestedChapterId).bookId(bookId).title("Exercises")
                .parentChapterId(chapterId).build();
        when(loader.loadChapter(chapterId, userId)).thenReturn(new BookExportData(
                book,
                List.of(chapter, nestedChapter),
                Map.of(),
                Map.of(),
                Map.of()
        ));

        PdfDocumentResult result = generator.generateChapter(chapterId, userId);

        assertThat(result.fileName()).isEqualTo("study-notes-arrays.pdf");
        assertThat(result.content()).startsWith("%PDF".getBytes());
    }

    @Test
    void rendersStructuredReadingSheetBlocks() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).title("Technical Analysis").author("Reader").topic(BookTopic.FINANCE_INVESTING).build();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).title("Indicators").build();
        List<ContentBlock> blocks = List.of(
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.HEADING).headingLevel(HeadingLevel.TITLE)
                        .content("Momentum").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.CODE).description("Calculation example")
                        .codeLanguage("javascript").content("const rsi = 70;").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.QUESTION_ANSWER).content("What does RSI measure?")
                        .answer("Momentum").build()
        );
        when(loader.load(bookId, userId)).thenReturn(new BookExportData(
                book, List.of(chapter), Map.of(chapterId, blocks), Map.of(), Map.of()
        ));

        PdfDocumentResult result = generator.generate(bookId, userId);

        assertThat(result.content()).startsWith("%PDF".getBytes());
        assertThat(result.content()).hasSizeGreaterThan(1_000);
    }

    @Test
    void rendersNewBlocksAndChapterHeaders() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).title("Reading summary").author("Reader").topic(BookTopic.OTHER).build();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).title("Small daily decisions").orderIndex(0).build();
        Chapter second = Chapter.builder().id(secondId).bookId(bookId).title("Habits and consistency").orderIndex(1).build();
        List<ContentBlock> blocks = List.of(
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.CALLOUT).content("Small choices compound over time.").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.QUOTE).content("Consistency is the key to progress.").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.TABLE).content("[[\"Habit\",\"Frequency\",\"Goal\"],"
                                + "[\"Reading\",\"Daily\",\"10 pages\"],[\"Exercise\",\"Weekly\",\"3 sessions\"]]").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.TODO_LIST)
                        .content("[{\"text\":\"Read ten pages\",\"checked\":true},"
                                + "{\"text\":\"Practice a new habit\",\"checked\":false}]").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.DIVIDER).content("").build(),
                ContentBlock.builder().id(UUID.randomUUID()).chapterId(chapterId)
                        .type(ContentBlockType.EXERCISE).resolved(true)
                        .content("Write down the excuses that prevent progress and choose a concrete action to overcome each one. "
                                + "Repeat this exercise at the end of each week to review progress.").build()
        );
        var imageBlock = ContentBlock.builder().id(UUID.randomUUID()).chapterId(secondId)
                .type(ContentBlockType.IMAGE).build();
        var attachment = com.marginalia.api.domain.Attachment.builder().id(UUID.randomUUID())
                .contentBlockId(imageBlock.getId()).url("https://res.cloudinary.com/test/image/upload/example.png").build();
        var image = new java.awt.image.BufferedImage(600, 150, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, 600, 150);
        graphics.setColor(new java.awt.Color(44, 95, 62));
        graphics.fillRoundRect(10, 10, 580, 130, 20, 20);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 30));
        graphics.drawString("Imagen incrustada en el resumen", 45, 85);
        graphics.dispose();
        var imageOutput = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", imageOutput);
        when(imageDownloader.downloadForEmbedding(attachment)).thenReturn(java.util.Optional.of(imageOutput.toByteArray()));
        when(loader.load(bookId, userId)).thenReturn(new BookExportData(
                book, List.of(chapter, second), Map.of(chapterId, blocks, secondId, List.of(imageBlock)),
                Map.of(), Map.of(imageBlock.getId(), List.of(attachment))
        ));
        PdfDocumentResult result = generator.generate(bookId, userId);
        assertThat(result.content()).startsWith("%PDF".getBytes());
        java.nio.file.Files.write(java.nio.file.Path.of("target/reading-blocks-preview.pdf"), result.content());
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
