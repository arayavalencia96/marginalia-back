package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.domain.HeadingLevel;
import com.marginalia.api.exception.PdfExportGenerationException;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Renders a complete owned book into an in-memory PDF document. */
@Service
@RequiredArgsConstructor
public class BookPdfGenerator {

    private static final float INDENT_SIZE = 18f;
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 22, Font.BOLD);
    private static final Font META_FONT = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.DARK_GRAY);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 11);
    private static final Font BODY_BOLD_FONT = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font CONTENT_TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font CONTENT_SUBTITLE_FONT = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font MONO_FONT = new Font(Font.COURIER, 9);
    private static final Font MONO_BOLD_FONT = new Font(Font.COURIER, 9, Font.BOLD);

    private final BookExportDataLoader dataLoader;
    private final CloudinaryImageDownloader imageDownloader;

    /**
     * Loads and renders an owned book as a PDF.
     *
     * @param bookId identifier of the book to render
     * @param userId identifier of the expected owner
     * @return generated PDF bytes and download filename
     * @throws PdfExportGenerationException if the document cannot be rendered
     */
    public PdfDocumentResult generate(UUID bookId, UUID userId) {
        BookExportData data = dataLoader.load(bookId, userId);
        return render(
                data.book(),
                data.chapters(),
                data.blocksByChapter(),
                data.stepsByBlock(),
                data.attachmentsByBlock(),
                fileName(data.book().getTitle())
        );
    }

    /**
     * Loads and renders one owned chapter and its nested chapters as a PDF document.
     *
     * @param chapterId identifier of the chapter that starts the export
     * @param userId identifier of the expected owner
     * @return generated PDF bytes and download filename
     */
    public PdfDocumentResult generateChapter(UUID chapterId, UUID userId) {
        BookExportData data = dataLoader.loadChapter(chapterId, userId);
        Chapter chapter = data.chapters().stream()
                .filter(candidate -> candidate.getId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new PdfExportGenerationException(
                        new IllegalStateException("Chapter was not found in export data")
                ));
        return render(
                data.book(),
                data.chapters(),
                data.blocksByChapter(),
                data.stepsByBlock(),
                data.attachmentsByBlock(),
                fileName(data.book().getTitle(), chapter.getTitle())
        );
    }

    private PdfDocumentResult render(
            Book book,
            List<Chapter> chapters,
            Map<UUID, List<ContentBlock>> blocksByChapter,
            Map<UUID, List<ContentBlockStep>> stepsByBlock,
            Map<UUID, List<Attachment>> attachmentsByBlock,
            String fileName
    ) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(document, output);
            document.addTitle(book.getTitle());
            document.addAuthor(book.getAuthor());
            document.open();
            addBookHeader(document, book);
            addChapterTree(document, chapters, blocksByChapter, stepsByBlock, attachmentsByBlock);
            document.close();
            return new PdfDocumentResult(output.toByteArray(), fileName);
        } catch (Exception exception) {
            if (document.isOpen()) {
                document.close();
            }
            throw new PdfExportGenerationException(exception);
        }
    }

    private void addBookHeader(Document document, Book book) throws DocumentException {
        Paragraph title = new Paragraph(book.getTitle(), TITLE_FONT);
        title.setSpacingAfter(8);
        document.add(title);

        Paragraph metadata = new Paragraph(
                "Autor: " + book.getAuthor(),
                META_FONT
        );
        metadata.setSpacingAfter(24);
        document.add(metadata);
    }

    private void addChapterTree(
            Document document,
            List<Chapter> chapters,
            Map<UUID, List<ContentBlock>> blocksByChapter,
            Map<UUID, List<ContentBlockStep>> stepsByBlock,
            Map<UUID, List<Attachment>> attachmentsByBlock
    ) throws DocumentException {
        Set<UUID> chapterIds = chapters.stream().map(Chapter::getId).collect(Collectors.toSet());
        Map<UUID, List<Chapter>> children = chapters.stream()
                .filter(chapter -> chapter.getParentChapterId() != null)
                .collect(Collectors.groupingBy(Chapter::getParentChapterId));
        children.values().forEach(list -> list.sort(
                Comparator.comparingInt(Chapter::getOrderIndex).thenComparing(Chapter::getId)
        ));

        List<Chapter> roots = chapters.stream()
                .filter(chapter -> chapter.getParentChapterId() == null
                        || !chapterIds.contains(chapter.getParentChapterId()))
                .toList();
        Set<UUID> rendered = new HashSet<>();
        for (Chapter root : roots) {
            addChapter(document, root, 0, "C" + (root.getOrderIndex() + 1),
                    children, rendered, blocksByChapter, stepsByBlock, attachmentsByBlock);
        }
        for (Chapter chapter : chapters) {
            if (!rendered.contains(chapter.getId())) {
                addChapter(document, chapter, 0, "C" + (chapter.getOrderIndex() + 1),
                        children, rendered, blocksByChapter, stepsByBlock, attachmentsByBlock);
            }
        }
    }

    private void addChapter(
            Document document,
            Chapter chapter,
            int depth,
            String marker,
            Map<UUID, List<Chapter>> children,
            Set<UUID> rendered,
            Map<UUID, List<ContentBlock>> blocksByChapter,
            Map<UUID, List<ContentBlockStep>> stepsByBlock,
            Map<UUID, List<Attachment>> attachmentsByBlock
    ) throws DocumentException {
        if (!rendered.add(chapter.getId())) {
            return;
        }

        Font headingFont = new Font(Font.HELVETICA, Math.max(12, 17 - depth), Font.BOLD);
        Paragraph heading = new Paragraph(marker + "  |  " + chapter.getTitle(), headingFont);
        heading.setIndentationLeft(depth * INDENT_SIZE);
        heading.setSpacingBefore(depth == 0 ? 12 : 8);
        heading.setSpacingAfter(6);
        heading.setKeepTogether(true);
        PdfPCell chapterCell = new PdfPCell(heading);
        chapterCell.setBackgroundColor(new Color(237, 240, 243));
        chapterCell.setBorder(Rectangle.NO_BORDER);
        chapterCell.setPadding(10);
        PdfPTable chapterHeader = new PdfPTable(1);
        chapterHeader.setWidthPercentage(100);
        chapterHeader.setSpacingBefore(20);
        chapterHeader.setSpacingAfter(12);
        chapterHeader.setKeepTogether(true);
        chapterHeader.addCell(chapterCell);
        document.add(chapterHeader);

        for (ContentBlock block : blocksByChapter.getOrDefault(chapter.getId(), List.of())) {
            addBlock(document, block, depth + 1, stepsByBlock, attachmentsByBlock);
        }
        for (Chapter child : children.getOrDefault(chapter.getId(), List.of())) {
            addChapter(document, child, depth + 1, marker + "." + (child.getOrderIndex() + 1),
                    children, rendered, blocksByChapter, stepsByBlock,
                    attachmentsByBlock);
        }
    }

    private void addBlock(
            Document document,
            ContentBlock block,
            int depth,
            Map<UUID, List<ContentBlockStep>> stepsByBlock,
            Map<UUID, List<Attachment>> attachmentsByBlock
    ) throws DocumentException {
        float indentation = depth * INDENT_SIZE;
        if (block.getDescription() != null
                && block.getType() != ContentBlockType.EXERCISE
                && block.getType() != ContentBlockType.IMAGE) {
            addParagraph(document, block.getDescription(), indentation);
        }
        switch (block.getType()) {
            case DIVIDER -> {
                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(Color.LIGHT_GRAY);
                cell.setFixedHeight(8);
                PdfPTable divider = new PdfPTable(1);
                divider.setWidthPercentage(100);
                divider.setSpacingAfter(12);
                divider.addCell(cell);
                document.add(divider);
            }
            case CALLOUT, QUOTE -> {
                PdfPCell cell = new PdfPCell(new Paragraph(block.getContent(), BODY_FONT));
                cell.setBorder(Rectangle.LEFT);
                cell.setBorderWidthLeft(3);
                cell.setBorderColor(Color.GRAY);
                cell.setPadding(10);
                if (block.getType() == ContentBlockType.CALLOUT) {
                    cell.setBackgroundColor(new Color(245, 243, 232));
                }
                PdfPTable panel = new PdfPTable(1);
                panel.setWidthPercentage(95);
                panel.setSpacingAfter(10);
                panel.addCell(cell);
                document.add(panel);
            }
            case TABLE -> {
                var rows = StructuredBlockContent.parse(block.getType(), block.getContent());
                PdfPTable table = new PdfPTable(rows.get(0).size());
                table.setWidthPercentage(95);
                table.setSpacingAfter(10);
                for (var row : rows) {
                    for (var value : row) {
                        PdfPCell cell = new PdfPCell(new Paragraph(value.asText(), BODY_FONT));
                        cell.setPadding(6);
                        cell.setBorderColor(Color.LIGHT_GRAY);
                        table.addCell(cell);
                    }
                }
                document.add(table);
            }
            case TODO_LIST -> {
                for (var item : StructuredBlockContent.parse(block.getType(), block.getContent())) {
                    addTodoItem(document, item.path("text").asText(), item.path("checked").asBoolean());
                }
            }
            case NOTE -> addParagraph(document, block.getContent(), indentation);
            case HEADING -> addContentHeading(document, block, indentation);
            case STEP_LIST -> addStepList(
                    document,
                    block,
                    indentation,
                    stepsByBlock.getOrDefault(block.getId(), List.of())
            );
            case CODE -> addCode(document, block, indentation);
            case MATH -> addParagraph(document, "Math: " + block.getContent(), indentation);
            case EXERCISE -> {
                addExercise(document, block, indentation);
                addLabeledDetail(document, "Resolution", block.getDescription(), indentation);
            }
            case QUESTION_ANSWER -> addQuestionAnswer(document, block, indentation);
            case IMAGE -> {
                addImages(document, indentation, attachmentsByBlock.getOrDefault(block.getId(), List.of()));
                addLabeledDetail(document, "Description", block.getDescription(), indentation);
            }
        }
    }

    private void addLabeledDetail(Document document, String label, String value, float indentation)
            throws DocumentException {
        if (value == null || value.isBlank()) {
            return;
        }
        Paragraph detail = new Paragraph();
        detail.add(new Chunk(label + ": ", BODY_BOLD_FONT));
        detail.add(new Chunk(value, BODY_FONT));
        detail.setIndentationLeft(indentation);
        detail.setSpacingAfter(8);
        document.add(detail);
    }

    private void addParagraph(Document document, String text, float indentation) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, BODY_FONT);
        paragraph.setIndentationLeft(indentation);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
    }

    private void addContentHeading(Document document, ContentBlock block, float indentation)
            throws DocumentException {
        Font font = block.getHeadingLevel() == HeadingLevel.TITLE
                ? CONTENT_TITLE_FONT
                : CONTENT_SUBTITLE_FONT;
        Paragraph heading = new Paragraph(block.getContent(), font);
        heading.setIndentationLeft(indentation);
        heading.setSpacingBefore(block.getHeadingLevel() == HeadingLevel.TITLE ? 10 : 6);
        heading.setSpacingAfter(6);
        document.add(heading);
    }

    private void addTodoItem(Document document, String text, boolean checked) throws DocumentException {
        PdfPCell cell = new PdfPCell(new Paragraph(text, BODY_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingLeft(22);
        cell.setPaddingTop(3);
        cell.setPaddingBottom(7);
        cell.setCellEvent((target, position, canvases) -> {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            float x = position.getLeft() + 3;
            float y = position.getTop() - 14;
            canvas.saveState();
            canvas.setColorStroke(checked ? new Color(44, 95, 62) : Color.GRAY);
            canvas.setLineWidth(1);
            canvas.rectangle(x, y, 11, 11);
            canvas.stroke();
            if (checked) {
                canvas.moveTo(x + 2, y + 5);
                canvas.lineTo(x + 5, y + 2);
                canvas.lineTo(x + 9, y + 9);
                canvas.stroke();
            }
            canvas.restoreState();
        });
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(95);
        table.addCell(cell);
        document.add(table);
    }

    private void addExercise(Document document, ContentBlock block, float indentation) throws DocumentException {
        addParagraph(document, block.getContent(), indentation);
    }

    private void addQuestionAnswer(Document document, ContentBlock block, float indentation)
            throws DocumentException {
        Paragraph question = new Paragraph();
        question.add(new Chunk("Pregunta: ", BODY_BOLD_FONT));
        question.add(new Chunk(block.getContent(), BODY_FONT));
        question.setIndentationLeft(indentation);
        question.setSpacingAfter(4);
        document.add(question);

        Paragraph answer = new Paragraph();
        answer.add(new Chunk("Respuesta: ", BODY_BOLD_FONT));
        answer.add(new Chunk(block.getAnswer(), BODY_FONT));
        answer.setIndentationLeft(indentation + 12);
        answer.setSpacingAfter(10);
        document.add(answer);
    }

    private void addStepList(
            Document document,
            ContentBlock block,
            float indentation,
            List<ContentBlockStep> steps
    ) throws DocumentException {
        for (int index = 0; index < steps.size(); index++) {
            String marker = switch (block.getStepStyle()) {
                case ALPHABETIC -> alphabeticMarker(index) + ".";
                case BULLETED -> "•";
                case NUMERIC -> (index + 1) + ".";
            };
            addParagraph(document, marker + " " + steps.get(index).getText(), indentation);
        }
    }

    private void addCode(Document document, ContentBlock block, float indentation) throws DocumentException {
        Paragraph code = new Paragraph();
        code.add(new Chunk("Language: " + block.getCodeLanguage() + "\n", MONO_BOLD_FONT));
        code.add(new Chunk(block.getContent(), MONO_FONT));

        PdfPCell cell = new PdfPCell(code);
        cell.setBackgroundColor(new Color(245, 245, 244));
        cell.setBorderColor(new Color(214, 211, 209));
        cell.setPadding(10);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(Math.max(60, 100 - indentation / 3));
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingAfter(10);
        table.addCell(cell);
        document.add(table);
    }

    private void addImages(Document document, float indentation, List<Attachment> attachments)
            throws DocumentException {
        if (attachments.isEmpty()) {
            addParagraph(document, "[No image attachment]", indentation);
            return;
        }

        for (Attachment attachment : attachments) {
            boolean embedded = imageDownloader.downloadForEmbedding(attachment)
                    .map(bytes -> addInlineImage(document, bytes, indentation))
                    .orElse(false);
            if (!embedded) {
                throw new DocumentException("No se pudo incluir una imagen. Reintenta la exportacion.");
            }
        }
    }

    private boolean addInlineImage(Document document, byte[] bytes, float indentation) {
        try {
            Image image = Image.getInstance(bytes);
            image.scaleToFit(420, 500);
            image.setIndentationLeft(indentation);
            image.setSpacingAfter(10);
            document.add(image);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private String alphabeticMarker(int index) {
        StringBuilder marker = new StringBuilder();
        int value = index + 1;
        while (value > 0) {
            value--;
            marker.append((char) ('a' + value % 26));
            value /= 26;
        }
        return marker.reverse().toString();
    }

    private String fileName(String... titleParts) {
        String title = String.join("-", titleParts);
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return (normalized.isBlank() ? "book" : normalized) + ".pdf";
    }
}
