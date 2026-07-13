package com.pdfdancer.client.rest;

import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextStyleE2ETest extends BaseTest {
    private static final Path IOWA_1040 =
            Path.of("src/test/examples/corpus-pdfs-failed/pdfs_all/ia_scr_1040.pdf");

    @Test
    void documentWideLiteralStylePersistsSavedPdfText() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("Before we begin")
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .size(14)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, the "Before we begin" text should appear with the requested red fill color and larger font size.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Before we begin", 1)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void pageScopedStyleReturnsCountsOnlyForSelectedPageInRealTaxFormPdf() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(1).text().style(
                TextStyleRequest.literal("Iowa")
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, the selected page-1 "Iowa" text should appear red; page 2 should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Iowa", 3, 1)
                .assertPdfTextOccurrenceCount("Iowa", 11, 2)
                .assertPdfTextContains("2012 IA 1040, page 2", 2);
    }

    @Test
    void regexStyleReturnsExpectedCountsAndPreservesSavedPdfText() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.regex("\\bB2B\\b")
                        .fillColor(PdfColorRequest.gray(0.1))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, the single "B2B" phrase should use the requested gray fill color.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("B2B", 1)
                .assertPdfTextContains("consumer products");
    }

    @Test
    void runsWhereTextContainsStyleReturnsExpectedCountsAndPreservesSavedPdfText() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.runsWhere()
                        .whereTextContains("B2B")
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, the run containing "B2B" should use the requested red fill color.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("B2B", 1)
                .assertPdfTextContains("consumer products");
    }

    @Test
    void caseInsensitiveLiteralStyleReturnsExpectedCounts() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(1).text().style(
                TextStyleRequest.literal("iowa")
                        .caseSensitive(false)
                        .fillColor(PdfColorRequest.rgb(0, 0, 1))
                        .build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, the selected page-1 case-insensitive "Iowa" matches should appear blue.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Iowa", 3, 1)
                .assertPdfTextOccurrenceCount("Iowa", 11, 2);
    }

    @Test
    void wholeWordsLiteralStyleDoesNotSelectSubstrings() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("book")
                        .wholeWords(true)
                        .fillColor(PdfColorRequest.rgb(0, 0.5, 0))
                        .build());

        assertEquals(4, response.matched());
        assertEquals(4, response.changed());
        // Visually, standalone "book" words should appear green; plural "books" text should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("books", 9)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void maxMatchesLimitsStyleChanges() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("workbook")
                        .maxMatches(2)
                        .fillColor(PdfColorRequest.rgb(0.5, 0, 0.5))
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, only the first two "workbook" matches should receive the requested purple fill color.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("workbook", 11)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void fontAndSpacingStyleFieldsAreAcceptedAndSavedPdfTextRemainsReadable() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("Before we begin")
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
        // Visually, "Before we begin" should use the requested bold font, size, colors, and spacing overrides.
        new PDFAssertions(pdf)
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void resetSpacingOverridesStyleFieldIsAcceptedAndSavedPdfTextRemainsReadable() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("Before we begin")
                        .resetSpacingOverrides(true)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        assertTrue(response.errors() == null || response.errors().isEmpty());
        // Visually, "Before we begin" should keep its readable text while backend spacing overrides are reset.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Before we begin", 1)
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void explicitSourceAnchoredLayoutStyleReturnsInspectibleResponseAndSavedPdfContent() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("Assumptions")
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .sourceAnchored()
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, both "Assumptions" headings should appear red in their source-anchored positions.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Assumptions", 2)
                .assertPdfTextContains("Before we begin");
    }

    @Test
    void reflowWhenSupportedLayoutStyleReturnsInspectibleResponseAndSavedPdfContent() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("Before we begin")
                        .fillColor(PdfColorRequest.rgb(0, 0, 1))
                        .reflowWhenSupported(TextLayoutRequest.Profile.BODY_TEXT)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        assertTrue(response.errors() == null || response.errors().isEmpty());
        // Visually, "Before we begin" should appear blue and surrounding body text should remain readable after reflow.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Before we begin", 1)
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void noMatchReturnsZeroAndLeavesSavedPdfContentUnchanged() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().style(
                TextStyleRequest.literal("DOES_NOT_EXIST_IN_WORKBOOK")
                        .fillColor(PdfColorRequest.rgb(1, 0, 0))
                        .build());

        assertEquals(0, response.matched());
        assertEquals(0, response.changed());
        // Visually, the saved workbook should be unchanged because no source text matched.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Sales Pitch", 9)
                .assertPdfTextContains("Obviously Awesome");
    }
}
