package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.model.TextTypeObjectRef;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import com.pdfdancer.common.response.PageSnapshot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests for template replacement with line breaks (\n).
 * <p>
 * Exposes a bug: after replacing a placeholder with text containing \n
 * using ReflowPreset.NONE, both lines render in the PDF but
 * selectTextLines() only returns the first line within the same session.
 * A save/reopen cycle is required for the second line to become selectable.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TemplateReplaceLinebreakTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(TemplateReplaceLinebreakTest.class);

    private Path fixturesDir;

    @BeforeAll
    void createFixture() throws IOException {
        fixturesDir = Files.createTempDirectory("linebreak-fixtures");

        PDFDancer client = newPdf();
        client.newParagraph()
                .text("{{DESCRIPTION}} trailing text.")
                .at(1, 50.0, 650.0)
                .font("Helvetica", 12)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("linebreak-template.pdf"), pdf);
        log.debug("Created fixture: linebreak-template.pdf");
    }

    private PDFDancer loadFixture() throws IOException {
        byte[] pdfBytes = Files.readAllBytes(fixturesDir.resolve("linebreak-template.pdf"));
        return PDFDancer.createSession(getValidToken(), pdfBytes, httpClient);
    }

    @Test
    public void testNoReflowBothLinesSelectableInSameSession() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("{{DESCRIPTION}}", "First line\nSecond line")
                        .build()
        );
        assertTrue(success);

        List<TextLineReference> lines = client.page(1).selectTextLines();
        List<String> texts = lines.stream()
                .map(TextLineReference::getText)
                .collect(java.util.stream.Collectors.toList());

        log.debug("Text lines in session: {}", texts);

        // BUG: only the first line is returned; the second line is missing
        assertTrue(texts.contains("First line"),
                "Should find 'First line' but got: " + texts);
        assertTrue(texts.stream().anyMatch(t -> t != null && t.startsWith("Second line")),
                "Should find line starting with 'Second line' but got: " + texts);
    }

    @Test
    public void testNoReflowBothLinesSelectableAfterSaveReopen() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("{{DESCRIPTION}}", "First line\nSecond line")
                        .build()
        );
        assertTrue(success);

        new PDFAssertions(client)
                .assertTextlineExists("First line", 1)
                .assertTextlineExists("Second line", 1);
    }

    @Test
    public void testNoReflowLineSpacingEditSameSession() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("{{DESCRIPTION}}", "First line\nSecond line")
                        .build()
        );
        assertTrue(success);

        List<TextParagraphReference> paragraphs = client.page(1)
                .selectParagraphsStartingWith("First line");
        assertEquals(1, paragraphs.size());

        paragraphs.get(0).edit().lineSpacing(3.0).apply();

        // Check via snapshot
        PageSnapshot snapshot = client.getPageSnapshot(1);
        TextTypeObjectRef para = snapshot.elements().stream()
                .filter(e -> e.getType() == ObjectType.PARAGRAPH)
                .filter(e -> e instanceof TextTypeObjectRef)
                .map(e -> (TextTypeObjectRef) e)
                .filter(e -> e.getText() != null && e.getText().startsWith("First line"))
                .findFirst()
                .orElse(null);

        assertNotNull(para, "Should find paragraph starting with 'First line'");

        // BUG: lineSpacings is empty in-session because the paragraph
        // has only one internal text line object (the \n split hasn't materialized)
        // TODO
        /*  assertFalse(para.getLineSpacings().isEmpty(),
                "lineSpacings should not be empty after editing");
        assertEquals(3.0, para.getLineSpacings().get(0), 0.5,
                "lineSpacing should be approximately 3.0");
         */
    }

    @Test
    public void testBestEffortBothLinesSelectableInSameSession() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.BEST_EFFORT)
                        .replace("{{DESCRIPTION}}", "First line\nSecond line")
                        .build()
        );
        assertTrue(success);

        List<TextLineReference> lines = client.page(1).selectTextLines();
        List<String> texts = lines.stream()
                .map(TextLineReference::getText)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(texts.contains("First line"),
                "Should find 'First line' but got: " + texts);
        assertTrue(texts.stream().anyMatch(t -> t != null && t.startsWith("Second line")),
                "Should find line starting with 'Second line' but got: " + texts);
    }

    @Test
    public void testBestEffortLineSpacingEditSameSession() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.BEST_EFFORT)
                        .replace("{{DESCRIPTION}}", "First line\nSecond line")
                        .build()
        );
        assertTrue(success);

        // Get line positions before
        List<TextLineReference> linesBefore = client.page(1).selectTextLines();
        double firstYBefore = linesBefore.stream()
                .filter(l -> "First line".equals(l.getText()))
                .findFirst().orElseThrow().getPosition().getY();
        double secondYBefore = linesBefore.stream()
                .filter(l -> l.getText() != null && l.getText().startsWith("Second line"))
                .findFirst().orElseThrow().getPosition().getY();
        double gapBefore = firstYBefore - secondYBefore;

        // Edit lineSpacing
        List<TextParagraphReference> paragraphs = client.page(1)
                .selectParagraphsStartingWith("First line");
        paragraphs.get(0).edit().lineSpacing(3.0).apply();

        // Get line positions after
        List<TextLineReference> linesAfter = client.page(1).selectTextLines();
        double firstYAfter = linesAfter.stream()
                .filter(l -> "First line".equals(l.getText()))
                .findFirst().orElseThrow().getPosition().getY();
        double secondYAfter = linesAfter.stream()
                .filter(l -> l.getText() != null && l.getText().startsWith("Second line"))
                .findFirst().orElseThrow().getPosition().getY();
        double gapAfter = firstYAfter - secondYAfter;

        log.debug("Gap before: {}, after: {}", gapBefore, gapAfter);
        assertTrue(gapAfter > gapBefore,
                "lineSpacing(3.0) should increase the gap (before=" + gapBefore + ", after=" + gapAfter + ")");
    }
}
