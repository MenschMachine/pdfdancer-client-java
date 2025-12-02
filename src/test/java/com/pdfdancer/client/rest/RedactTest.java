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

    @Test
    public void fluentRedactParagraph() {
        PDFDancer pdf = createClient();

        boolean result = pdf.page(1).selectParagraphsMatching(".*Obvious.*").get(0)
                .redact()
                .withReplacement("[REDACTED]")
                .withColor(Color.BLACK)
                .apply();

        assertTrue(result);
    }

    @Test
    public void fluentRedactTextLine() {
        PDFDancer pdf = createClient();

        boolean result = pdf.page(1).selectTextLinesMatching(".*Obvious.*").get(0)
                .redact()
                .withReplacement("***")
                .apply();

        assertTrue(result);
    }

    @Test
    public void fluentRedactImage() {
        PDFDancer pdf = createClient();

        boolean result = pdf.page(1).selectImages().get(0)
                .redact()
                .withColor(new Color(128, 128, 128))
                .apply();

        assertTrue(result);
    }

    @Test
    public void fluentRedactWithDefaults() {
        PDFDancer pdf = createClient();

        boolean result = pdf.page(1).selectTextLinesMatching(".*Obvious.*").get(0)
                .redact()
                .apply();

        assertTrue(result);
    }

    @Test
    public void fluentRedactMultipleWithForEach() {
        PDFDancer pdf = createClient();

        var textLines = pdf.page(1).selectTextLinesMatching(".*");
        assertTrue(textLines.size() >= 3);

        for (int i = 0; i < 3; i++) {
            boolean result = textLines.get(i)
                    .redact()
                    .withReplacement("[REMOVED]")
                    .apply();
            assertTrue(result);
        }
    }
}
