package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;
import com.marginalia.api.domain.StepStyle;
import com.marginalia.api.exception.PdfExportGenerationException;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Anchor;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
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

@Service
@RequiredArgsConstructor
public class BookPdfGenerator {

    private static final float INDENT_SIZE = 18f;
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 22, Font.BOLD);
    private static final Font META_FONT = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.DARK_GRAY);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 11);
    private static final Font MONO_FONT = new Font(Font.COURIER, 9);
    private static final Font MONO_BOLD_FONT = new Font(Font.COURIER, 9, Font.BOLD);
    private static final Font LINK_FONT = new Font(Font.HELVETICA, 10, Font.UNDERLINE, new Color(37, 99, 235));

    private final BookExportDataLoader dataLoader;
    private final CloudinaryImageDownloader imageDownloader;

    public PdfDocumentResult generate(UUID bookId, UUID userId) {
        BookExportData data = dataLoader.load(bookId, userId);
        return render(
                data.book(),
                data.chapters(),
                data.blocksByChapter(),
                data.stepsByBlock(),
                data.attachmentsByBlock()
        );
    }

    private PdfDocumentResult render(
            Book book,
            List<Chapter> chapters,
            Map<UUID, List<ContentBlock>> blocksByChapter,
            Map<UUID, List<ContentBlockStep>> stepsByBlock,
            Map<UUID, List<Attachment>> attachmentsByBlock
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
            return new PdfDocumentResult(output.toByteArray(), fileName(book));
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
                "Author: " + book.getAuthor() + "  |  Topic: " + book.getTopic(),
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
            addChapter(document, root, 0, children, rendered, blocksByChapter, stepsByBlock, attachmentsByBlock);
        }
        for (Chapter chapter : chapters) {
            if (!rendered.contains(chapter.getId())) {
                addChapter(document, chapter, 0, children, rendered, blocksByChapter, stepsByBlock, attachmentsByBlock);
            }
        }
    }

    private void addChapter(
            Document document,
            Chapter chapter,
            int depth,
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
        Paragraph heading = new Paragraph(chapter.getTitle(), headingFont);
        heading.setIndentationLeft(depth * INDENT_SIZE);
        heading.setSpacingBefore(depth == 0 ? 12 : 8);
        heading.setSpacingAfter(6);
        document.add(heading);

        for (ContentBlock block : blocksByChapter.getOrDefault(chapter.getId(), List.of())) {
            addBlock(document, block, depth + 1, stepsByBlock, attachmentsByBlock);
        }
        for (Chapter child : children.getOrDefault(chapter.getId(), List.of())) {
            addChapter(document, child, depth + 1, children, rendered, blocksByChapter, stepsByBlock,
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
        switch (block.getType()) {
            case NOTE -> addParagraph(document, block.getContent(), indentation);
            case STEP_LIST -> addStepList(
                    document,
                    block,
                    indentation,
                    stepsByBlock.getOrDefault(block.getId(), List.of())
            );
            case CODE -> addCode(document, block, indentation);
            case MATH -> addParagraph(document, "Math: " + block.getContent(), indentation);
            case EXERCISE -> addParagraph(
                    document,
                    (block.isResolved() ? "[x] " : "[ ] ") + block.getContent(),
                    indentation
            );
            case IMAGE -> addImages(document, indentation, attachmentsByBlock.getOrDefault(block.getId(), List.of()));
        }
    }

    private void addParagraph(Document document, String text, float indentation) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, BODY_FONT);
        paragraph.setIndentationLeft(indentation);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
    }

    private void addStepList(
            Document document,
            ContentBlock block,
            float indentation,
            List<ContentBlockStep> steps
    ) throws DocumentException {
        for (int index = 0; index < steps.size(); index++) {
            String marker = block.getStepStyle() == StepStyle.ALPHABETIC
                    ? alphabeticMarker(index) + "."
                    : (index + 1) + ".";
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
                addImageLink(document, attachment.getUrl(), indentation);
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

    private void addImageLink(Document document, String url, float indentation) throws DocumentException {
        Anchor link = new Anchor("Open image", LINK_FONT);
        link.setReference(url);
        Paragraph paragraph = new Paragraph();
        paragraph.add(link);
        paragraph.setIndentationLeft(indentation);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
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

    private String fileName(Book book) {
        String normalized = Normalizer.normalize(book.getTitle(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return (normalized.isBlank() ? "book" : normalized) + ".pdf";
    }
}
