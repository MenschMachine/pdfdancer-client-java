package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.request.RedactRequest;
import com.pdfdancer.common.request.RedactTarget;
import com.pdfdancer.common.response.RedactResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RedactTest extends BaseTest {

    @Test
    public void redactTextLineByPattern() {
        PDFDancer pdf = createClient();

        Position position = Position.atPage(1);
        position.setTextPattern(".*Obvious.*");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.TEXT_LINE, position)
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
        assertTrue(response.count() > 0);
    }

    @Test
    public void redactParagraphByPattern() {
        PDFDancer pdf = createClient();

        Position position = Position.atPage(1);
        position.setTextPattern(".*positioning.*");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("***")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.PARAGRAPH, position)
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactImageOnPage() {
        PDFDancer pdf = createClient();

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("")
                .placeholderColor(new Color(128, 128, 128))
                .addTarget(ObjectType.IMAGE, Position.atPage(1))
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactMultipleTargetsInSingleRequest() {
        PDFDancer pdf = createClient();

        Position textPosition = Position.atPage(1);
        textPosition.setTextPattern(".*Obvious.*");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.TEXT_LINE, textPosition, "[TEXT REMOVED]")
                .addTarget(ObjectType.IMAGE, Position.atPage(1))
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactWithCustomReplacementPerTarget() {
        PDFDancer pdf = createClient();

        Position pos1 = Position.atPage(1);
        pos1.setTextPattern(".*Obviously.*");

        Position pos2 = Position.atPage(1);
        pos2.setTextPattern(".*Awesome.*");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[DEFAULT]")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.TEXT_LINE, pos1, "[FIRST]")
                .addTarget(ObjectType.TEXT_LINE, pos2, "[SECOND]")
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactWithCustomPlaceholderColor() {
        PDFDancer pdf = createClient();

        Color redColor = new Color(255, 0, 0);

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("")
                .placeholderColor(redColor)
                .addTarget(ObjectType.IMAGE, Position.atPage(1))
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactNoMatchReturnsZeroCount() {
        PDFDancer pdf = createClient();

        Position position = Position.atPage(1);
        position.setTextPattern("THIS_PATTERN_DOES_NOT_EXIST_ANYWHERE_12345");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.TEXT_LINE, position)
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals(0, response.count());
    }

    @Test
    public void redactOnSpecificPageOnly() {
        PDFDancer pdf = createClient();

        Position page2Position = Position.atPage(2);
        page2Position.setTextPattern(".*");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[PAGE2]")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.TEXT_LINE, page2Position)
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactPathObjects() {
        PDFDancer pdf = createClient();

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("")
                .placeholderColor(new Color(0, 0, 0))
                .addTarget(ObjectType.PATH, Position.atPage(1))
                .build();

        RedactResponse response = pdf.redact(request);

        assertNotNull(response);
        assertTrue(response.success());
    }

    @Test
    public void redactUsingRedactTargetDirectly() {
        PDFDancer pdf = createClient();

        Position position = Position.atPage(1);
        position.setTextPattern(".*Obvious.*");

        RedactTarget target = new RedactTarget(ObjectType.TEXT_LINE, position, "[DIRECT]");

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

        Position position = Position.atPage(1);
        position.setTextPattern(".*Obvious.*");

        RedactRequest request = RedactRequest.builder()
                .defaultReplacement("[REDACTED]")
                .placeholderColor(Color.BLACK)
                .addTarget(ObjectType.TEXT_LINE, position)
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
