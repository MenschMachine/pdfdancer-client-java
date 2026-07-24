package com.pdfdancer.client.rest;

import com.pdfdancer.common.request.TextDeleteRequest;
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextDeleteE2ETest extends BaseTest {
    private static final Path IOWA_1040 =
            Path.of("src/test/examples/corpus-pdfs-failed/pdfs_all/ia_scr_1040.pdf");

    @Test
    void documentWideDeletePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("Sales Pitch").build());

        assertEquals(9, response.matched());
        assertEquals(9, response.changed());
        // Visually, every "Sales Pitch" label should be removed from the workbook.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Sales Pitch", 0)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void pageScopedDeletePersistsOnlyOnSelectedPageInRealTaxFormPdf() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(1).text().delete(
                TextDeleteRequest.literal("Iowa").build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, page 1 should no longer show the selected "Iowa" text; page 2 should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Iowa", 0, 1)
                .assertPdfTextOccurrenceCount("Iowa", 11, 2)
                .assertPdfTextContains("2012 IA 1040, page 2", 2);
    }

    @Test
    void regexDeletePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.regex("\\bB2B\\b")
                        .layout(TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, the single "B2B" phrase should disappear while nearby body text remains readable.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("B2B", 0)
                .assertPdfTextContains("consumer products");
    }

    @Test
    void caseInsensitiveLiteralDeletePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("april dunford")
                        .caseSensitive(false)
                        .build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, every case-insensitive author-name match should be removed from the workbook.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("April Dunford", 0)
                .assertPdfTextContains("aprildunford.com");
    }

    @Test
    void wholeWordsLiteralDeleteDoesNotDeleteSubstringsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("book")
                        .wholeWords(true)
                        .build());

        assertEquals(4, response.matched());
        assertEquals(4, response.changed());
        // Visually, standalone "book" words should disappear; plural "books" text should remain.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("books", 9)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void maxMatchesLimitsSavedPdfDeletes() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("workbook")
                        .maxMatches(2)
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, only the first two "workbook" matches should be removed; later matches should remain visible.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("workbook", 9)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void explicitSourceAnchoredLayoutDeletePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("Assumptions")
                        .sourceAnchored()
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, both "Assumptions" headings should be removed from their original positions.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Assumptions", 0)
                .assertPdfTextContains("Before we begin");
    }

    @Test
    void reflowWhenSupportedLayoutReturnsInspectibleResponseAndSavedPdfContent() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("Assumptions")
                        .reflowWhenSupported(TextLayoutRequest.Profile.BODY_TEXT)
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        assertTrue(response.errors() == null || response.errors().isEmpty());
        // Visually, both "Assumptions" headings should be removed and surrounding text should remain readable after reflow.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Assumptions", 0)
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void noMatchReturnsZeroAndLeavesSavedPdfContentUnchanged() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().delete(
                TextDeleteRequest.literal("DOES_NOT_EXIST_IN_WORKBOOK")
                        .build());

        assertEquals(0, response.matched());
        assertEquals(0, response.changed());
        // Visually, the saved workbook should be unchanged because no source text matched.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Sales Pitch", 9)
                .assertPdfTextContains("Obviously Awesome");
    }
}
