package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplaceRequest;
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
 * Integration tests for template replacement functionality.
 * Creates fixture PDFs with placeholder text and tests actual API operations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TemplateIntegrationTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(TemplateIntegrationTest.class);

    private Path fixturesDir;

    /**
     * Creates all template fixture PDFs once before any tests run.
     */
    @BeforeAll
    void createTemplateFixtures() throws IOException {
        fixturesDir = Files.createTempDirectory("template-fixtures");
        log.debug("Created fixtures directory: {}", fixturesDir);

        createFixtureWithTwoPlaceholders();
        createFixtureWithMultiPagePlaceholder();
        createFixtureWithDuplicatePlaceholders();
        createFixtureWithoutPlaceholders();
    }

    private void createFixtureWithTwoPlaceholders() throws IOException {
        PDFDancer client = newPdf();

        client.newParagraph()
                .text("__FIRSTNAME__")
                .at(1, 100.0, 700.0)
                .font("Helvetica", 12)
                .add();

        client.newParagraph()
                .text("__LASTNAME__")
                .at(1, 100.0, 650.0)
                .font("Helvetica", 12)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("two-placeholders.pdf"), pdf);
        log.debug("Created fixture: two-placeholders.pdf");
    }

    private void createFixtureWithMultiPagePlaceholder() throws IOException {
        PDFDancer client = newPdf();
        client.addPage();

        client.newParagraph()
                .text("__TITLE__")
                .at(1, 100.0, 700.0)
                .font("Helvetica", 14)
                .add();

        client.newParagraph()
                .text("__TITLE__")
                .at(2, 100.0, 700.0)
                .font("Helvetica", 14)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("multipage-placeholder.pdf"), pdf);
        log.debug("Created fixture: multipage-placeholder.pdf");
    }

    private void createFixtureWithDuplicatePlaceholders() throws IOException {
        PDFDancer client = newPdf();

        client.newParagraph()
                .text("__NAME__")
                .at(1, 100.0, 700.0)
                .font("Helvetica", 12)
                .add();

        client.newParagraph()
                .text("__NAME__")
                .at(1, 100.0, 650.0)
                .font("Helvetica", 12)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("duplicate-placeholders.pdf"), pdf);
        log.debug("Created fixture: duplicate-placeholders.pdf");
    }

    private void createFixtureWithoutPlaceholders() throws IOException {
        PDFDancer client = newPdf();

        client.newParagraph()
                .text("Regular text")
                .at(1, 100.0, 700.0)
                .font("Helvetica", 12)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("no-placeholders.pdf"), pdf);
        log.debug("Created fixture: no-placeholders.pdf");
    }

    private PDFDancer loadFixture(String fixtureName) throws IOException {
        byte[] pdfBytes = Files.readAllBytes(fixturesDir.resolve(fixtureName));
        return PDFDancer.createSession(getValidToken(), pdfBytes, httpClient);
    }

    @Test
    public void testDocumentLevelTemplateReplacement() throws IOException {
        // Given: A pre-created PDF fixture with two placeholders
        PDFDancer client = loadFixture("two-placeholders.pdf");

        // Debug: verify placeholders exist before replacement
        List<TextParagraphReference> paragraphsBefore = client.page(1).selectParagraphs();
        log.debug("Paragraphs before replacement: {}", paragraphsBefore.size());
        paragraphsBefore.forEach(p -> log.debug("  - {}", p.getText()));

        // When: Performing template replacement
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .replace("__FIRSTNAME__", "John")
                        .replace("__LASTNAME__", "Doe")
                        .build()
        );

        // Then: Replacement succeeds and placeholders are replaced
        assertTrue(success, "Template replacement should succeed");
        new PDFAssertions(client)
                .assertParagraphExists("John", 1)
                .assertParagraphExists("Doe", 1)
                .assertParagraphNotExists("__FIRSTNAME__", 1)
                .assertParagraphNotExists("__LASTNAME__", 1);
    }

    @Test
    public void testPageLevelTemplateReplacement() throws IOException {
        // Given: A pre-created PDF fixture with same placeholder on different pages
        PDFDancer client = loadFixture("multipage-placeholder.pdf");

        // When: Replacing only on page 1
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .replace("__TITLE__", "Chapter 1")
                        .pageIndex(0)
                        .build()
        );

        // Then: Only page 1 is replaced, page 2 retains placeholder
        assertTrue(success, "Page-level template replacement should succeed");
        new PDFAssertions(client)
                .assertParagraphExists("Chapter 1", 1)
                .assertParagraphNotExists("__TITLE__", 1)
                .assertParagraphExists("__TITLE__", 2);
    }

    @Test
    public void testMultipleOccurrencesReplacement() throws IOException {
        // Given: A pre-created PDF fixture with duplicate placeholders
        PDFDancer client = loadFixture("duplicate-placeholders.pdf");

        // When: Replacing the placeholder
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .replace("__NAME__", "Alice")
                        .build()
        );

        // Then: All occurrences are replaced
        assertTrue(success, "Template replacement should succeed");
        PDFAssertions assertions = new PDFAssertions(client);
        assertions.assertParagraphNotExists("__NAME__", 1);

        // Verify both occurrences were replaced (2 paragraphs with "Alice")
        List<TextParagraphReference> paragraphs = client.page(1).selectParagraphs();
        long aliceCount = paragraphs.stream()
                .filter(p -> "Alice".equals(p.getText()))
                .count();
        assertEquals(2, aliceCount, "Should find 2 occurrences of 'Alice'");
    }

    @Test
    public void testMissingPlaceholderFails() throws IOException {
        // Given: A pre-created PDF fixture without placeholders
        PDFDancer client = loadFixture("no-placeholders.pdf");

        // When/Then: Attempting to replace non-existent placeholder fails
        assertThrows(Exception.class, () -> client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .replace("__MISSING__", "value")
                        .build()
        ));
    }

    @Test
    public void testSimpleReplacementWithExistingPdf() {
        // Given: Load a PDF with known text
        PDFDancer client = createClient("ObviouslyAwesome.pdf");

        // Verify session works with read operations
        List<TextParagraphReference> before = client.page(1).selectParagraphs();
        log.debug("Found {} paragraphs", before.size());
        assertFalse(before.isEmpty(), "Should have paragraphs");

        // When: Replacing known text via template replacement
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .replace("Obvious", "Amazing")
                        .build()
        );

        // Then: Replacement succeeds
        assertTrue(success, "Template replacement should succeed");
    }

    @Test
    public void testWithReflowPresetBestEffort() throws IOException {
        // Given: A PDF with a short placeholder
        PDFDancer client = loadFixture("two-placeholders.pdf");

        // When: Explicitly using BEST_EFFORT preset
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.BEST_EFFORT)
                        .replace("__FIRSTNAME__", "A very long first name that requires reflow")
                        .replace("__LASTNAME__", "Doe")
                        .build()
        );

        // Then: Replacement succeeds
        assertTrue(success, "Template replacement with BEST_EFFORT should succeed");
    }

    @Test
    public void testWithoutReflow() throws IOException {
        // Given: A PDF with placeholders
        PDFDancer client = loadFixture("two-placeholders.pdf");

        // When: Using NONE preset (no reflow)
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("__FIRSTNAME__", "John")
                        .replace("__LASTNAME__", "Doe")
                        .build()
        );

        // Then: Replacement succeeds using legacy findAndReplace
        assertTrue(success, "Template replacement without reflow should succeed");
        new PDFAssertions(client)
                .assertParagraphExists("John", 1)
                .assertParagraphExists("Doe", 1);
    }

}
