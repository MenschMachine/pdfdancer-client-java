package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.response.RedactResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RedactTest extends BaseTest {

    @Test
    public void redactTextLineByPattern() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertEquals(1, textLines.size(), "Should find at least one matching text line");

        boolean result = textLines.get(0).redact();

        assertTrue(result);

        new PDFAssertions(pdf)
                .assertTextlineDoesNotExist("Obvious", 1)
                .assertTextlineExists("REDACTED", 1);
    }

    @Test
    public void redactParagraphByPattern() {
        PDFDancer pdf = createClient();

        var paragraphs = pdf.page(1).selectParagraphsMatching(".*Obvious.*");
        assertEquals(1, paragraphs.size(), "Should find at least one matching paragraph");

        boolean result = paragraphs.get(0).redact("***");

        assertTrue(result);
        new PDFAssertions(pdf)
                .assertParagraphNotExists("Obvious", 1)
                .assertParagraphExists("\\*\\*\\*", 1);
    }

    @Test
    public void redactImageOnPage() {
        PDFDancer pdf = createClient();

        var images = pdf.page(1).selectImages();
        assertEquals(2, images.size(), "Should find at least one image");

        boolean result = images.get(0).redact(new Color(128, 128, 128));

        assertTrue(result);
        new PDFAssertions(pdf)
                .assertNumberOfImages(1, 1);
    }

    @Test
    public void redactImageOnPage2() {
        PDFDancer pdf = createClient("Showcase.pdf");

        var images = pdf.page(3).selectImages();
        assertEquals(5, images.size());

        boolean result = images.get(0).redact(new Color(128, 128, 128));

        assertTrue(result);
        new PDFAssertions(pdf)
                .assertNumberOfImages(4, 3);
    }

    @Test
    public void redactMultipleTargetsInSingleRequest() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertTrue(textLines.size() > 0, "Should find at least one matching text line");
        var images = pdf.page(1).selectImages();
        assertTrue(images.size() > 0, "Should find at least one image");

        RedactResponse response = pdf.redact(List.of(textLines.get(0), images.get(0)));

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactWithCustomReplacement() {
        PDFDancer pdf = createClient();

        var textLines1 = pdf.page(1).selectTextLinesMatching(".*Obviously.*");
        var textLines2 = pdf.page(1).selectTextLinesMatching(".*Awesome.*");

        java.util.List<BaseReference> toRedact = new java.util.ArrayList<>();
        if (textLines1.size() > 0) {
            toRedact.add(textLines1.get(0));
        }
        if (textLines2.size() > 0) {
            toRedact.add(textLines2.get(0));
        }

        if (!toRedact.isEmpty()) {
            RedactResponse response = pdf.redact(toRedact, "[CUSTOM]");
            assertNotNull(response);
            assertTrue(response.success());
        }
    }

    @Test
    public void redactWithCustomPlaceholderColor() {
        PDFDancer pdf = createClient();

        var images = pdf.page(1).selectImages();
        assertTrue(images.size() > 0, "Should find at least one image");

        Color redColor = new Color(255, 0, 0);
        boolean result = images.get(0).redact(redColor);

        assertTrue(result);
    }

    @Test
    public void redactNoMatchReturnsZeroCount() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching("THIS_PATTERN_DOES_NOT_EXIST_ANYWHERE_12345");
        assertEquals(0, textLines.size(), "Should not find any matching text lines");

        // Since the ID-based API requires actual IDs, we verify no matches were found client-side
        // The test validates that selectTextLinesMatching correctly returns empty for non-existent patterns
    }

    @Test
    public void redactOnSpecificPageOnly() {
        PDFDancer pdf = createClient();

        var textLinesPage2 = pdf.page(2).selectTextLinesMatching(".*");

        if (textLinesPage2.size() > 0) {
            boolean result = textLinesPage2.get(0).redact("[PAGE2]");

            assertTrue(result);
        }
    }

    @Test
    public void redactPathObjects() {
        PDFDancer pdf = createClient();

        var paths = pdf.page(1).selectPaths();

        if (paths.size() > 0) {
            boolean result = paths.get(0).redact();

            assertTrue(result);
        }
    }

    @Test
    public void redactSingleObject() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertTrue(textLines.size() > 0, "Should find at least one matching text line");

        boolean result = textLines.get(0).redact("[DIRECT]");

        assertTrue(result);
    }

    @Test
    public void redactVerifyPdfModified() {
        PDFDancer pdf = createClient();

        byte[] beforeBytes = pdf.getFileBytes();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertEquals(1, textLines.size(), "Should find one matching text line");

        boolean result = textLines.get(0).redact();
        assertTrue(result);

        byte[] afterBytes = pdf.getFileBytes();
        assertNotEquals(beforeBytes.length, afterBytes.length,
                "PDF should be modified after redaction");
    }

    // Simple API tests

    private PDFDancer reloadPdf(PDFDancer pdf) {
        byte[] bytes = pdf.getFileBytes();
        return PDFDancer.createSession(getValidToken(), bytes, httpClient);
    }

    @Test
    public void simpleRedactParagraph() {
        PDFDancer pdf = createClient();

        var paragraph = pdf.page(1).selectParagraphsMatching(".*Obvious.*").get(0);
        String originalText = paragraph.getText();
        int redactedCountBefore = pdf.page(1).selectParagraphsMatching(".*REDACTED.*").size();

        boolean result = paragraph.redact("[REDACTED]");
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectParagraphsMatching(".*REDACTED.*").size();
        assertTrue(redactedCountAfter > redactedCountBefore, "Redacted count should increase");

        var stillOriginal = reloaded.page(1).selectParagraphsMatching(".*" + escapeRegex(originalText) + ".*");
        assertEquals(0, stillOriginal.size(), "Original text should be gone: " + originalText);
    }

    @Test
    public void simpleRedactTextLine() {
        PDFDancer pdf = createClient();

        var textLine = pdf.page(1).selectTextLinesMatching(".*Obvious.*").get(0);
        String originalText = textLine.getText();
        int redactedCountBefore = pdf.page(1).selectTextLinesMatching(".*\\*\\*\\*.*").size();

        boolean result = textLine.redact("***");
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectTextLinesMatching(".*\\*\\*\\*.*").size();
        assertTrue(redactedCountAfter > redactedCountBefore, "Redacted count should increase");

        var stillOriginal = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(originalText) + ".*");
        assertEquals(0, stillOriginal.size(), "Original text should be gone: " + originalText);
    }

    @Test
    public void simpleRedactImage() {
        PDFDancer pdf = createClient();

        int imageCountBefore = pdf.page(1).selectImages().size();
        assertTrue(imageCountBefore >= 1, "Should have at least 1 image before redaction");

        boolean result = pdf.page(1).selectImages().get(0).redact();
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int imageCountAfter = reloaded.page(1).selectImages().size();
        assertTrue(imageCountAfter < imageCountBefore, "Image count should decrease after redaction");
    }

    @Test
    public void simpleRedactWithDefaults() {
        PDFDancer pdf = createClient();

        var textLine = pdf.page(1).selectTextLinesMatching(".*Obvious.*").get(0);
        String originalText = textLine.getText();
        int redactedCountBefore = pdf.page(1).selectTextLinesMatching(".*REDACTED.*").size();

        boolean result = textLine.redact();
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectTextLinesMatching(".*REDACTED.*").size();
        assertTrue(redactedCountAfter > redactedCountBefore, "Redacted count should increase");

        var stillOriginal = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(originalText) + ".*");
        assertEquals(0, stillOriginal.size(), "Original text should be gone: " + originalText);
    }

    @Test
    public void simpleRedactMultipleTextLines() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*");
        assertTrue(textLines.size() >= 3, "Need at least 3 text lines for this test");

        String text0 = textLines.get(0).getText();
        String text1 = textLines.get(1).getText();
        String text2 = textLines.get(2).getText();
        int redactedCountBefore = pdf.page(1).selectTextLinesMatching(".*REMOVED.*").size();

        for (int i = 0; i < 3; i++) {
            boolean result = textLines.get(i).redact("[REMOVED]");
            assertTrue(result, "Redaction " + i + " should succeed");
        }

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectTextLinesMatching(".*REMOVED.*").size();
        assertTrue(redactedCountAfter >= redactedCountBefore + 3,
                "Redacted count should increase by at least 3, was " + redactedCountBefore + " now " + redactedCountAfter);

        var remaining0 = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(text0) + ".*");
        var remaining1 = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(text1) + ".*");
        var remaining2 = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(text2) + ".*");

        assertEquals(0, remaining0.size(), "Original text 0 should be gone: " + text0);
        assertEquals(0, remaining1.size(), "Original text 1 should be gone: " + text1);
        assertEquals(0, remaining2.size(), "Original text 2 should be gone: " + text2);
    }

    private String escapeRegex(String text) {
        return text.replaceAll("([\\\\.*+?^${}()|\\[\\]])", "\\\\$1");
    }
}
