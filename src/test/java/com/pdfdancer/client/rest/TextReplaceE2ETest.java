package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.PdfAffineTransform;
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.response.TextEditChangeDiagnostic;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextReplaceE2ETest extends BaseTest {
    private static final Path IOWA_1040 =
            Path.of("src/test/examples/corpus-pdfs-failed/pdfs_all/ia_scr_1040.pdf");

    @Test
    void documentWideReplacePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("Sales Pitch", "Revenue Pitch").build());

        assertEquals(9, response.matched());
        assertEquals(9, response.changed());
        // Visually, every "Sales Pitch" label in the workbook should read "Revenue Pitch".
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Sales Pitch", 0)
                .assertPdfTextOccurrenceCount("Revenue Pitch", 9)
                .assertPdfTextContains("Obviously Awesome")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void pageScopedReplacePersistsOnlyOnSelectedPageInRealTaxFormPdf() throws IOException {
        byte[] sourcePdf = Files.readAllBytes(IOWA_1040);
        PDFDancer pdf = PDFDancer.createSession(getValidToken(), sourcePdf, httpClient);

        TextEditResponse response = pdf.page(1).text().replace(
                TextReplaceRequest.literal("Iowa", "Hawkeye").build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, page 1 should show "Hawkeye" where it previously showed "Iowa"; page 2 should still show "Iowa".
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Iowa", 0, 1)
                .assertPdfTextOccurrenceCount("Hawkeye", 3, 1)
                .assertPdfTextOccurrenceCount("Iowa", 11, 2)
                .assertPdfTextOccurrenceCount("Hawkeye", 0, 2)
                .assertPdfTextContains("2012 IA 1040, page 2", 2);
    }

    @Test
    void regexReplacePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.regex("\\bB2B\\b", "Business-to-business")
                        .layout(TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT))
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, the single "B2B" phrase should expand to "Business-to-business" without disturbing nearby body text.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("B2B", 0)
                .assertPdfTextOccurrenceCount("Business-to-business", 1)
                .assertPdfTextContains("consumer products");
    }

    @Test
    void caseInsensitiveLiteralReplacePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("april dunford", "April D.")
                        .caseSensitive(false)
                        .build());

        assertEquals(3, response.matched());
        assertEquals(3, response.changed());
        // Visually, each author-name occurrence should be shortened from "April Dunford" to "April D.".
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("April Dunford", 0)
                .assertPdfTextOccurrenceCount("April D.", 3)
                .assertPdfTextContains("aprildunford.com");
    }

    @Test
    void wholeWordsLiteralReplaceDoesNotReplaceSubstringsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("book", "monograph")
                        .wholeWords(true)
                        .build());

        assertEquals(4, response.matched());
        assertEquals(4, response.changed());
        // Visually, standalone "book" words should read "monograph"; plural "books" text should remain unchanged.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("monograph", 4)
                .assertPdfTextOccurrenceCount("books", 9)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void maxMatchesLimitsSavedPdfChanges() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("workbook", "worksheet")
                        .maxMatches(2)
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, only the first two "workbook" matches should read "worksheet"; later matches should still read "workbook".
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("worksheet", 2)
                .assertPdfTextOccurrenceCount("workbook", 9)
                .assertPdfTextContains("This Workbook");
    }

    @Test
    void emptyReplacementDeletesMatchedTextInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("Please do not distribute this workbook", "")
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        // Visually, the distribution-warning sentence should disappear while the surrounding workbook content remains.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Please do not distribute this workbook", 0)
                .assertPdfTextContains("I like to make updates")
                .assertPdfTextContains("April Dunford");
    }

    @Test
    void imageReplacementPersistsAtCaretRelativeGeometry() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.builder()
                        .literal("Please do not distribute this workbook")
                        .maxMatches(1)
                        .replaceWithImage(
                                new File("src/test/resources/fixtures/logo-80.png"),
                                PdfAffineTransform.builder()
                                        .scale(20, 10)
                                        .translate(3, -2)
                                        .build())
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed());
        assertEquals(1, response.change().size());
        TextEditChangeDiagnostic change = response.change().get(0);
        assertEquals("replaceWithImage", change.operation());
        assertEquals("Please do not distribute this workbook", change.sourceText());
        assertEquals("", change.resultText());
        assertNotNull(change.generatedElementIds());
        assertEquals(1, change.generatedElementIds().size());

        String imageId = change.generatedElementIds().get(0);
        ImageReference liveImage = pdf.page(change.page()).selectImages().stream()
                .filter(image -> imageId.equals(image.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Generated image is not selectable: " + imageId));
        assertEquals(20.0, liveImage.getWidth(), 1.0);
        assertEquals(10.0, liveImage.getHeight(), 1.0);

        PDFAssertions persisted = new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Please do not distribute this workbook", 0);
        assertFalse(persisted.getPdf().page(change.page()).selectImages().stream()
                .filter(image -> imageId.equals(image.getInternalId()))
                .toList()
                .isEmpty(), "Generated image must persist in the saved PDF");
    }

    @Test
    void replaceWithCustomFontRejectsUnencodableGlyph() {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");
        pdf.registerFont(new File("src/test/resources/fixtures/SourceSans3-Regular.ttf"));
        pdf.text().style(TextStyleRequest.literal("Before we begin")
                .font("SourceSans3-Regular")
                .size(12)
                .build());

        PdfDancerClientException ex = assertThrows(PdfDancerClientException.class, () ->
                pdf.text().replace(TextReplaceRequest.literal("Before we begin", "\uF020").build()));
        assertTrue(ex.getMessage().contains("No glyph for U+F020"));
    }

    @Test
    void explicitSourceAnchoredLayoutReplacePersistsInSavedWorkbookPdf() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("Assumptions", "Operating Context")
                        .sourceAnchored()
                        .build());

        assertEquals(2, response.warnings().size());
        assertTrue(response.warnings().get(0).message().contains("used for replacement text 'Operating Context'; source font was"));
        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        // Visually, both "Assumptions" headings should read "Operating Context" in their original positions.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Assumptions", 0)
                .assertPdfTextOccurrenceCount("Operating Context", 2)
                .assertPdfTextContains("Before we begin");
    }

    @Test
    void reflowWhenSupportedLayoutReturnsInspectibleResponseAndSavedPdfContent() {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("Assumptions", "Context")
                        .reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                        .build());

        assertEquals(2, response.matched());
        assertEquals(2, response.changed());
        assertTrue(response.warnings().isEmpty(), response.warnings().toString());
        assertTrue(response.errors().isEmpty(), response.warnings().toString());
        // Visually, both "Assumptions" headings should read "Context" and the surrounding body layout should remain readable.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("Assumptions", 0)
                .assertPdfTextOccurrenceCount("Context", 2)
                .assertPdfTextContains("Differentiated Value");
    }

    @Test
    void noMatchReturnsZeroAndLeavesSavedPdfContentUnchanged() throws IOException {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal("DOES_NOT_EXIST_IN_WORKBOOK", "SHOULD_NOT_APPEAR")
                        .build());

        assertEquals(0, response.matched());
        assertEquals(0, response.changed());
        // Visually, the saved workbook should be unchanged because no source text matched.
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount("SHOULD_NOT_APPEAR", 0)
                .assertPdfTextOccurrenceCount("Sales Pitch", 9)
                .assertPdfTextContains("Obviously Awesome");
    }
}
