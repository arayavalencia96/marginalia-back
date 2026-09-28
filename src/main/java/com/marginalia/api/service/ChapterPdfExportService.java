package com.marginalia.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Generates a PDF for one owned chapter and its nested chapters. */
@Service
@RequiredArgsConstructor
public class ChapterPdfExportService {

    private final BookPdfGenerator bookPdfGenerator;

    /**
     * Generates the selected chapter tree as a PDF.
     *
     * @param chapterId identifier of the chapter that starts the export
     * @param userId identifier of the expected owner
     * @return generated PDF bytes and file name
     */
    public PdfDocumentResult export(UUID chapterId, UUID userId) {
        return bookPdfGenerator.generateChapter(chapterId, userId);
    }
}
