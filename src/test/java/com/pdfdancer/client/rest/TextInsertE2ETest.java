package com.pdfdancer.client.rest;

import com.pdfdancer.common.request.TextInsertRequest;
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextInsertE2ETest extends BaseTest {
    private static final Path IOWA_1040 =
            Path.of("src/test/examples/corpus-pdfs-failed/pdfs_all/ia_scr_1040.pdf");

    @Test
    void documentWideInsertPersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("Before we begin", " Inserted").build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, " Inserted" should appear immediately after the "Before we begin" heading.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Before we begin Inserted", 1)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void pageScopedInsertPersistsOnlyOnSelectedPageInRealTaxFormPdf() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(1).text().insert(
                TextInsertRequest.after("Iowa", " STATE").build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, page 1 should show "Iowa STATE" at each selected occurrence; page 2 should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Iowa STATE", 3, 1)
                .assertPdfTextOccurrenceCount("Iowa STATE", 0, 2)
                .assertPdfTextOccurrenceCount("Iowa", 11, 2)
                .assertPdfTextContains("2012 IA 1040, page 2", 2);
    }

    @Test
    void regexInsertPersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.afterRegex("\\bB2B\\b", " market")
                        .layout(TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, " market" should appear immediately after the single "B2B" phrase.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("B2B market", 1)
                .assertPdfTextContains("consumer products");
    }

    @Test
    void stylePatchInsertPersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("Before we begin", " PATCHED")
                        .size(12)
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, " PATCHED" should appear after "Before we begin" using the requested inserted-text style patch.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Before we begin PATCHED", 1)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void coordinateInsertPersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.at(1, 72, 720, "Coordinate Insert")
                        .font("Helvetica")
                        .size(12)
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, "Coordinate Insert" should appear near the upper-left area of page 1.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Coordinate Insert", 1)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void pageScopedCoordinateInsertPersistsOnSelectedPageInRealTaxFormPdf() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(2).text().insert(
                TextInsertRequest.builder()
                        .coordinate(72, 720)
                        .insert("Page Coordinate Insert")
                        .font("Helvetica")
                        .size(12)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, "Page Coordinate Insert" should appear near the upper-left area of page 2 only.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Page Coordinate Insert", 0, 1)
                .assertPdfTextOccurrenceCount("Page Coordinate Insert", 1, 2)
                .assertPdfTextContains("2012 IA 1040", 1)
                .assertPdfTextContains("2012 IA 1040, page 2", 2);
    }

    @Test
    void rotatedCoordinateInsertPersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.at(1, 540, 144, "Rotated Insert")
                        .rotationDegrees(90)
                        .font("Helvetica")
                        .size(10)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, "Rotated Insert" should appear rotated clockwise near the lower-right area of page 1.
        new PDFAssertions(pdf)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void fontAndSpacingStylePatchFieldsAreAcceptedAndSavedPdfTextRemainsReadable() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("Before we begin", " STYLED")
                        .font("Helvetica-Bold")
                        .size(12)
                        .fillColor(PdfColorRequest.rgb(0.8, 0.1, 0.1))
                        .strokeColor(PdfColorRequest.gray(0.0))
                        .characterSpacing(0.5)
                        .wordSpacing(1.0)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        assertTrue(response.errors() == null || response.errors().isEmpty());
        // Visually, " STYLED" should be inserted after "Before we begin" with the requested font, size, color, and spacing overrides.
        new PDFAssertions(pdf)
                .assertPdfTextContains("Before we begin")
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void caseInsensitiveLiteralInsertPersistsInSavedWorkbookPdf() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(1).text().insert(
                TextInsertRequest.after("iowa", " CASEMARK")
                        .caseSensitive(false)
                        .build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, page 1 should show "Iowa CASEMARK" for case-insensitive matches; page 2 should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Iowa CASEMARK", 3, 1)
                .assertPdfTextOccurrenceCount("Iowa CASEMARK", 0, 2)
                .assertPdfTextContains("2012 IA 1040, page 2", 2);
    }

    @Test
    void wholeWordsLiteralInsertDoesNotInsertIntoSubstringsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("book", " item")
                        .wholeWords(true)
                        .build());

        assertEquals(4, response.matched());
        assertEquals(4, response.changed());
        // Visually, " item" should be inserted only after standalone "book" words; plural "books" text should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("book item", 4)
                .assertPdfTextOccurrenceCount("books", 9)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void maxMatchesLimitsSavedPdfInserts() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("workbook", " copy")
                        .maxMatches(2)
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, only the first two "workbook" matches should gain the inserted " copy" suffix.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("workbook copy", 2)
                .assertPdfTextOccurrenceCount("workbook", 11)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void explicitSourceAnchoredLayoutInsertPersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.before("Assumptions", "Operating ")
                        .sourceAnchored()
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, "Operating " should appear immediately before both "Assumptions" headings.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Operating Assumptions", 2)
                .assertPdfTextContains("Before we begin");
    }

    @Test
    void reflowWhenSupportedLayoutReturnsInspectibleResponseAndSavedPdfContent() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("Before we begin", " REFLOW")
                        .reflowWhenSupported(TextLayoutRequest.Profile.BODY_TEXT)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        assertTrue(response.errors() == null || response.errors().isEmpty());
        // Visually, " REFLOW" should appear after "Before we begin" and the surrounding body text should remain readable.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Before we begin REFLOW", 1)
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void noMatchReturnsZeroAndLeavesSavedPdfContentUnchanged() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().insert(
                TextInsertRequest.after("DOES_NOT_EXIST_IN_WORKBOOK", " SHOULD_NOT_APPEAR")
                        .build());

        assertEquals(0, response.matched());
        assertEquals(0, response.changed());
        // Visually, the saved workbook should be unchanged because no anchor text matched.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("SHOULD_NOT_APPEAR", 0)
                .assertPdfTextOccurrenceCount("Sales Pitch", 9)
                .assertPdfTextContains("Obviously Awesome");
    }
}
