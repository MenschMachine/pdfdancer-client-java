package com.pdfdancer.client.rest;

import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.response.ReadingUnit;
import com.pdfdancer.common.response.ReadingUnitBounds;
import com.pdfdancer.common.response.TextEditResponse;
import com.pdfdancer.common.response.TextOperationDiagnostic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextAlignmentE2ETest extends BaseTest {
    private static final String SOURCE_LINE = "This line will be replaced.";
    private static final String MONOSPACE_LINE = "Monospace text: 0123456789 ABC xyz";
    /** The reading unit of Showcase.pdf that has two lines. Its first line is not its last line. */
    private static final String MONOSPACE_UNIT = MONOSPACE_LINE + "\nUnderlined text example";

    /** Returns the left and right edge of the one reading unit with exactly this text. */
    private static double[] lineExtent(PDFDancer pdf, String text) {
        List<ReadingUnit> units = pdf.page(1).analyzeReadingUnits().units().stream()
                .filter(unit -> unit.text() != null && unit.text().strip().equals(text))
                .toList();
        assertEquals(1, units.size(), "Expected one line '" + text + "', found " + units.size());
        ReadingUnitBounds bounds = units.get(0).provenance().bounds();
        return new double[]{bounds.x(), bounds.x() + bounds.width()};
    }

    // LEFT is not tested here. Without an alignment, the API also keeps the left edge,
    // so the test could not detect a dropped alignment field.
    @ParameterizedTest
    @EnumSource(value = TextLayoutRequest.Alignment.class, names = {"RIGHT", "CENTERED"})
    void replaceAlignmentKeepsTheReferenceOfTheEditedLine(TextLayoutRequest.Alignment alignment)
            throws IOException {
        PDFDancer pdf = createClient("Showcase.pdf");
        double[] source = lineExtent(pdf, SOURCE_LINE);

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal(SOURCE_LINE, "Short text.")
                        .maxMatches(1)
                        .alignment(alignment)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed(), response.toString());
        assertTrue(response.errors() == null || response.errors().isEmpty(), response.toString());
        double[] saved = lineExtent(new PDFAssertions(pdf).getPdf(), "Short text.");
        assertTrue(saved[1] - saved[0] < source[1] - source[0]);
        if (alignment == TextLayoutRequest.Alignment.RIGHT) {
            assertEquals(source[1], saved[1], 0.5);
        } else {
            assertEquals((source[0] + source[1]) / 2, (saved[0] + saved[1]) / 2, 0.5);
        }
    }

    @Test
    void replaceJustifiedKeepsBothEdgesOfTheEditedLine() throws IOException {
        PDFDancer pdf = createClient("Showcase.pdf");
        double[] source = lineExtent(pdf, MONOSPACE_UNIT);

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal(MONOSPACE_LINE, "Mono a b c d e")
                        .alignment(TextLayoutRequest.Alignment.JUSTIFIED)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed(), response.toString());
        assertTrue(response.errors() == null || response.errors().isEmpty(), response.toString());
        // The unit starts with the justified line. Without an alignment, the new text is narrower
        // and the right edge of the unit moves left. So this test fails if the alignment is dropped.
        double[] saved = lineExtent(new PDFAssertions(pdf).getPdf(), "Mono a b c d e\nUnderlined text example");
        assertEquals(source[0], saved[0], 0.5);
        assertEquals(source[1], saved[1], 0.5);
    }

    @Test
    void replaceAlignmentThatCannotBeHonoredForOneMatchStillChangesTheOtherMatch() throws IOException {
        PDFDancer pdf = createClient("Showcase.pdf");

        // One word has no word gaps. The first line of the two-line unit is not its last line,
        // so the API cannot justify it. The single line stays left-aligned and is not affected.
        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.regex("This line will be replaced\\.|Monospace text: 0123456789 ABC xyz", "Short.")
                        .alignment(TextLayoutRequest.Alignment.JUSTIFIED)
                        .build());

        assertEquals(2, response.matched());
        assertEquals(1, response.changed(), response.toString());
        assertEquals(List.of(TextOperationDiagnostic.ALIGNMENT_NOT_HONORED),
                response.errors().stream().map(error -> error.code()).toList(), response.toString());
        new PDFAssertions(pdf)
                .assertPdfTextOccurrenceCount(SOURCE_LINE, 0, 1)
                .assertPdfTextOccurrenceCount(MONOSPACE_LINE, 1, 1)
                .assertPdfTextOccurrenceCount("Short.", 1, 1);
    }

    @Test
    void replaceAlignmentThatCannotBeHonoredIsReportedWithoutThrowing() throws IOException {
        PDFDancer pdf = createClient("Showcase.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal(SOURCE_LINE, "Replacement with reflow.")
                        .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .alignment(TextLayoutRequest.Alignment.RIGHT)
                        .build());

        assertEquals(1, response.matched());
        assertEquals(0, response.changed(), response.toString());
        assertEquals(List.of(TextOperationDiagnostic.ALIGNMENT_NOT_HONORED),
                response.errors().stream().map(error -> error.code()).toList());
        new PDFAssertions(pdf).assertPdfTextOccurrenceCount(SOURCE_LINE, 1, 1);
    }

    @Test
    void replaceAlignmentCannotAddALine() throws IOException {
        PDFDancer pdf = createClient("Showcase.pdf");

        TextEditResponse response = pdf.text().replace(
                TextReplaceRequest.literal(SOURCE_LINE, "Line one.\nLine two.")
                        .alignment(TextLayoutRequest.Alignment.RIGHT)
                        .build());

        assertEquals(0, response.changed(), response.toString());
        assertEquals(List.of(TextOperationDiagnostic.ALIGNMENT_NOT_HONORED),
                response.errors().stream().map(error -> error.code()).toList());
        new PDFAssertions(pdf).assertPdfTextOccurrenceCount(SOURCE_LINE, 1, 1);
    }
}
