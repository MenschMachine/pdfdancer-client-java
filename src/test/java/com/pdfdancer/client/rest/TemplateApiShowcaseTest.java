package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplacement;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests showcasing all ways to use the fluent templating API.
 * Demonstrates the full range of API options for template replacement.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TemplateApiShowcaseTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(TemplateApiShowcaseTest.class);

    private Path fixturesDir;

    @BeforeAll
    void createFixtures() throws IOException {
        fixturesDir = Files.createTempDirectory("template-api-showcase");
        log.debug("Created fixtures directory: {}", fixturesDir);

        createFormFixture();
        createMultiPageFixture();
    }

    private void createFormFixture() throws IOException {
        PDFDancer client = newPdf();

        client.newParagraph()
                .text("{{name}}")
                .at(1, 100.0, 750.0)
                .font("Helvetica", 12)
                .add();

        client.newParagraph()
                .text("{{title}}")
                .at(1, 100.0, 700.0)
                .font("Helvetica", 12)
                .add();

        client.newParagraph()
                .text("{{email}}")
                .at(1, 100.0, 650.0)
                .font("Helvetica", 12)
                .add();

        client.newParagraph()
                .text("{{date}}")
                .at(1, 100.0, 600.0)
                .font("Helvetica", 12)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("form.pdf"), pdf);
        log.debug("Created fixture: form.pdf");
    }

    private void createMultiPageFixture() throws IOException {
        PDFDancer client = newPdf();
        client.addPage();

        client.newParagraph()
                .text("{{header}}")
                .at(1, 100.0, 750.0)
                .font("Helvetica", 14)
                .add();

        client.newParagraph()
                .text("{{header}}")
                .at(2, 100.0, 750.0)
                .font("Helvetica", 14)
                .add();

        byte[] pdf = client.getFileBytes();
        Files.write(fixturesDir.resolve("multipage.pdf"), pdf);
        log.debug("Created fixture: multipage.pdf");
    }

    private PDFDancer loadFixture(String name) throws IOException {
        byte[] pdfBytes = Files.readAllBytes(fixturesDir.resolve(name));
        return PDFDancer.createSession(getValidToken(), pdfBytes, httpClient);
    }

    // ========================================================================
    // WAY 1: Simplest - single replacement
    // ========================================================================

    @Test
    public void way1_simpleReplace() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        // Simplest possible API
        boolean success = client.replace("{{name}}", "John Doe").apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1);
    }

    // ========================================================================
    // WAY 2: With font
    // ========================================================================

    @Test
    public void way2_withFont() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        boolean success = client.replace("{{name}}", "John Doe")
                .withFont("Helvetica-Bold", 14.0)
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1);
    }

    // ========================================================================
    // WAY 3: With color
    // ========================================================================

    @Test
    public void way3_withColor() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        boolean success = client.replace("{{name}}", "John Doe")
                .withColor(255, 0, 0)  // Red
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1);
    }

    // ========================================================================
    // WAY 4: With font and color
    // ========================================================================

    @Test
    public void way4_withFontAndColor() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        boolean success = client.replace("{{name}}", "John Doe")
                .withFont("Helvetica-Bold", 16.0)
                .withColor(0, 0, 255)  // Blue
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1);
    }

    // ========================================================================
    // WAY 5: With Font and Color objects
    // ========================================================================

    @Test
    public void way5_withFontAndColorObjects() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        Font boldFont = new Font("Helvetica-Bold", 14.0);
        Color blueColor = new Color(0, 0, 255);

        boolean success = client.replace("{{name}}", "John Doe")
                .withFont(boldFont)
                .withColor(blueColor)
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1);
    }

    // ========================================================================
    // WAY 6: Multiple replacements chained
    // ========================================================================

    @Test
    public void way6_multipleReplacements() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        boolean success = client.replace("{{name}}", "John Doe")
                .replace("{{title}}", "Software Engineer")
                .replace("{{email}}", "john@example.com")
                .replace("{{date}}", "2024-01-15")
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1)
                .assertParagraphExists("Software Engineer", 1)
                .assertParagraphExists("john@example.com", 1)
                .assertParagraphExists("2024-01-15", 1);
    }

    // ========================================================================
    // WAY 7: Multiple replacements with different formatting
    // ========================================================================

    @Test
    public void way7_multipleWithDifferentFormatting() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        boolean success = client.replace("{{name}}", "John Doe")
                .withFont("Helvetica-Bold", 14.0)
                .withColor(0, 0, 0)
                .replace("{{title}}", "CEO")
                .withFont("Helvetica-Oblique", 12.0)
                .withColor(100, 100, 100)
                .replace("{{email}}", "john@company.com")
                .withColor(0, 0, 255)
                .replace("{{date}}", "2024-01-15")
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1)
                .assertParagraphExists("CEO", 1)
                .assertParagraphExists("john@company.com", 1)
                .assertParagraphExists("2024-01-15", 1);
    }

    // ========================================================================
    // WAY 8: With reflow preset
    // ========================================================================

    @Test
    public void way8_withReflow() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        boolean success = client.replace("{{name}}", "A Very Long Name That Might Need Reflow")
                .withReflow(ReflowPreset.BEST_EFFORT)
                .apply();

        assertTrue(success);
    }

    // ========================================================================
    // WAY 9: Page-specific via onPage()
    // ========================================================================

    @Test
    public void way9_pageSpecificViaOnPage() throws IOException {
        PDFDancer client = loadFixture("multipage.pdf");

        // Replace only on first page using onPage()
        boolean success = client.replace("{{header}}", "Page 1 Header")
                .withFont("Helvetica-Bold", 16.0)
                .onPage(1)
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("Page 1 Header", 1);
    }

    // ========================================================================
    // WAY 10: Page-specific via page().replace()
    // ========================================================================

    @Test
    public void way10_pageSpecificViaPageClient() throws IOException {
        PDFDancer client = loadFixture("multipage.pdf");

        // Replace only on first page using page()
        boolean success = client.page(1).replace("{{header}}", "Page 1 Title")
                .withFont("Helvetica-Bold", 18.0)
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("Page 1 Title", 1);
    }

    // ========================================================================
    // WAY 11: Full options - formatting + reflow + page
    // ========================================================================

    @Test
    public void way11_fullOptions() throws IOException {
        PDFDancer client = loadFixture("multipage.pdf");

        boolean success = client.replace("{{header}}", "Styled Header")
                .withFont("Helvetica-Bold", 18.0)
                .withColor(128, 0, 128)  // Purple
                .withReflow(ReflowPreset.BEST_EFFORT)
                .onPage(1)
                .apply();

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("Styled Header", 1);
    }

    // ========================================================================
    // WAY 12: Using applyReplacements() for programmatic use
    // ========================================================================

    @Test
    public void way12_programmaticWithApplyReplacements() throws IOException {
        PDFDancer client = loadFixture("form.pdf");

        // For complex programmatic scenarios, still use applyReplacements
        TemplateReplacement nameReplacement = TemplateReplacement.withFormatting(
                "{{name}}", "John Doe",
                new Font("Helvetica-Bold", 14.0),
                new Color(0, 0, 0)
        );

        TemplateReplacement titleReplacement = TemplateReplacement.of("{{title}}", "Director");

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .addReplacement(nameReplacement)
                        .addReplacement(titleReplacement)
                        .build()
        );

        assertTrue(success);
        new PDFAssertions(client)
                .assertParagraphExists("John Doe", 1)
                .assertParagraphExists("Director", 1);
    }
}
