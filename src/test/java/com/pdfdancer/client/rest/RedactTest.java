package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.request.RedactRequest;
import com.pdfdancer.common.request.RedactTarget;
import com.pdfdancer.common.response.RedactResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RedactTest extends BaseTest {

    @Test
    public void redactTextLineByPattern() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertTrue(textLines.size() > 0, "Should find at least one matching text line");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTargetById(textLines.get(0).getInternalId())
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
        assertTrue(response.count() > 0);
    }

    @Test
    public void redactParagraphByPattern() {
        PDFDancer pdf = createClient();

        var paragraphs = pdf.page(1).selectParagraphsMatching(".*Obvious.*");
        assertTrue(paragraphs.size() > 0, "Should find at least one matching paragraph");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("***")
                .placeholderColor(Color.BLACK)
                .addTargetById(paragraphs.get(0).getInternalId())
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactImageOnPage() {
        PDFDancer pdf = createClient();

        var images = pdf.page(1).selectImages();
        assertTrue(images.size() > 0, "Should find at least one image");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("")
                .placeholderColor(new Color(128, 128, 128))
                .addTargetById(images.get(0).getInternalId())
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactMultipleTargetsInSingleRequest() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertTrue(textLines.size() > 0, "Should find at least one matching text line");
        var images = pdf.page(1).selectImages();
        assertTrue(images.size() > 0, "Should find at least one image");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTargetById(textLines.get(0).getInternalId(), "[TEXT REMOVED]")
                .addTargetById(images.get(0).getInternalId())
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactWithCustomReplacementPerTarget() {
        PDFDancer pdf = createClient();

        var textLines1 = pdf.page(1).selectTextLinesMatching(".*Obviously.*");
        var textLines2 = pdf.page(1).selectTextLinesMatching(".*Awesome.*");

        RedactRequest.Builder builder = RedactRequest.builder()
                .defaultReplacement("[DEFAULT]")
                .placeholderColor(Color.BLACK);

        if (textLines1.size() > 0) {
            builder.addTargetById(textLines1.get(0).getInternalId(), "[FIRST]");
        }
        if (textLines2.size() > 0) {
            builder.addTargetById(textLines2.get(0).getInternalId(), "[SECOND]");
        }

        RedactResponse response = pdf.redact(builder.build());

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactWithCustomPlaceholderColor() {
        PDFDancer pdf = createClient();

        var images = pdf.page(1).selectImages();
        assertTrue(images.size() > 0, "Should find at least one image");

        Color redColor = new Color(255, 0, 0);

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("")
                .placeholderColor(redColor)
                .addTargetById(images.get(0).getInternalId())
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
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
            RedactRequest request = RedactRequest.builder()
                    .defaultReplacement("[PAGE2]")
                    .placeholderColor(Color.BLACK)
                    .addTargetById(textLinesPage2.get(0).getInternalId())
                    .build();

            RedactResponse response = pdf.redact(request);

            assertNotNull(response);
            assertTrue(response.success());
        }
    }

    @Test
    public void redactPathObjects() {
        PDFDancer pdf = createClient();

        var paths = pdf.page(1).selectPaths();

        if (paths.size() > 0) {
            RedactRequest request = RedactRequest.builder()
                    .defaultReplacement("")
                    .placeholderColor(new Color(0, 0, 0))
                    .addTargetById(paths.get(0).getInternalId())
                    .build();

            RedactResponse response = pdf.redact(request);

            assertNotNull(response);
            assertTrue(response.success());
        }
    }

    @Test
    public void redactUsingRedactTargetDirectly() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertTrue(textLines.size() > 0, "Should find at least one matching text line");

        RedactTarget target = new RedactTarget(textLines.get(0).getInternalId(), "[DIRECT]");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[DEFAULT]")
                .placeholderColor(Color.BLACK)
                .addTarget(target)
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactVerifyPdfModified() {
        PDFDancer pdf = createClient();

        byte[] beforeBytes = pdf.getFileBytes();

        var textLines = pdf.page(1).selectTextLinesMatching(".*Obvious.*");
        assertTrue(textLines.size() > 0, "Should find at least one matching text line");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTargetById(textLines.get(0).getInternalId())
                .build();

        RedactResponse response = pdf.redact(request);
        assertTrue(response.success());

        if (response.count() > 0) {
            byte[] afterBytes = pdf.getFileBytes();
            assertNotEquals(beforeBytes.length, afterBytes.length,
                    "PDF should be modified after redaction");
        }
    }

    // Fluent API tests

    private PDFDancer reloadPdf(PDFDancer pdf) {
        byte[] bytes = pdf.getFileBytes();
        return PDFDancer.createSession(getValidToken(), bytes, httpClient);
    }

    @Test
    public void fluentRedactParagraph() {
        PDFDancer pdf = createClient();

        var paragraph = pdf.page(1).selectParagraphsMatching(".*Obvious.*").get(0);
        String originalText = paragraph.getText();
        int redactedCountBefore = pdf.page(1).selectParagraphsMatching(".*REDACTED.*").size();

        boolean result = paragraph.redact()
                .withReplacement("[REDACTED]")
                .withColor(Color.BLACK)
                .apply();
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectParagraphsMatching(".*REDACTED.*").size();
        assertTrue(redactedCountAfter > redactedCountBefore, "Redacted count should increase");

        var stillOriginal = reloaded.page(1).selectParagraphsMatching(".*" + escapeRegex(originalText) + ".*");
        assertEquals(0, stillOriginal.size(), "Original text should be gone: " + originalText);
    }

    @Test
    public void fluentRedactTextLine() {
        PDFDancer pdf = createClient();

        var textLine = pdf.page(1).selectTextLinesMatching(".*Obvious.*").get(0);
        String originalText = textLine.getText();
        int redactedCountBefore = pdf.page(1).selectTextLinesMatching(".*\\*\\*\\*.*").size();

        boolean result = textLine.redact()
                .withReplacement("***")
                .apply();
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectTextLinesMatching(".*\\*\\*\\*.*").size();
        assertTrue(redactedCountAfter > redactedCountBefore, "Redacted count should increase");

        var stillOriginal = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(originalText) + ".*");
        assertEquals(0, stillOriginal.size(), "Original text should be gone: " + originalText);
    }

    @Test
    public void fluentRedactImage() {
        PDFDancer pdf = createClient();

        int imageCountBefore = pdf.page(1).selectImages().size();
        assertTrue(imageCountBefore >= 1, "Should have at least 1 image before redaction");

        boolean result = pdf.page(1).selectImages().get(0)
                .redact()
                .withColor(new Color(128, 128, 128))
                .apply();
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int imageCountAfter = reloaded.page(1).selectImages().size();
        assertTrue(imageCountAfter < imageCountBefore, "Image count should decrease after redaction");
    }

    @Test
    public void fluentRedactWithDefaults() {
        PDFDancer pdf = createClient();

        var textLine = pdf.page(1).selectTextLinesMatching(".*Obvious.*").get(0);
        String originalText = textLine.getText();
        int redactedCountBefore = pdf.page(1).selectTextLinesMatching(".*REDACTED.*").size();

        boolean result = textLine.redact().apply();
        assertTrue(result);

        PDFDancer reloaded = reloadPdf(pdf);
        int redactedCountAfter = reloaded.page(1).selectTextLinesMatching(".*REDACTED.*").size();
        assertTrue(redactedCountAfter > redactedCountBefore, "Redacted count should increase");

        var stillOriginal = reloaded.page(1).selectTextLinesMatching(".*" + escapeRegex(originalText) + ".*");
        assertEquals(0, stillOriginal.size(), "Original text should be gone: " + originalText);
    }

    @Test
    public void fluentRedactMultipleTextLines() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*");
        assertTrue(textLines.size() >= 3, "Need at least 3 text lines for this test");

        String text0 = textLines.get(0).getText();
        String text1 = textLines.get(1).getText();
        String text2 = textLines.get(2).getText();
        int redactedCountBefore = pdf.page(1).selectTextLinesMatching(".*REMOVED.*").size();

        for (int i = 0; i < 3; i++) {
            boolean result = textLines.get(i)
                    .redact()
                    .withReplacement("[REMOVED]")
                    .apply();
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
