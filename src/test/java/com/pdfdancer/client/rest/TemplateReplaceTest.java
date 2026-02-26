package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.model.Size;
import com.pdfdancer.common.request.TemplateReplacement;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for template replacement functionality.
 */
public class TemplateReplaceTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(TemplateReplaceTest.class);

    @Test
    public void templateReplacementBuilderWorks() {
        TemplateReplacement replacement = TemplateReplacement.of("{{name}}", "John Doe");

        assertEquals("{{name}}", replacement.placeholder());
        assertEquals("John Doe", replacement.text());
        assertNull(replacement.font());
        assertNull(replacement.color());
    }

    @Test
    public void templateReplacementWithFont() {
        Font font = new Font("Helvetica", 12.0);
        TemplateReplacement replacement = TemplateReplacement.withFont("{{title}}", "Dr.", font);

        assertEquals("{{title}}", replacement.placeholder());
        assertEquals("Dr.", replacement.text());
        assertNotNull(replacement.font());
        assertEquals("Helvetica", replacement.font().getName());
        assertNull(replacement.color());
    }

    @Test
    public void templateReplacementWithColor() {
        Color green = new Color(0, 255, 0);
        TemplateReplacement replacement = TemplateReplacement.withColor("{{status}}", "ACTIVE", green);

        assertEquals("{{status}}", replacement.placeholder());
        assertEquals("ACTIVE", replacement.text());
        assertNull(replacement.font());
        assertNotNull(replacement.color());
    }

    @Test
    public void templateReplacementWithFormatting() {
        Font font = new Font("Arial", 14.0);
        Color blue = new Color(0, 0, 255);
        TemplateReplacement replacement = TemplateReplacement.withFormatting("{{header}}", "Title", font, blue);

        assertEquals("{{header}}", replacement.placeholder());
        assertEquals("Title", replacement.text());
        assertNotNull(replacement.font());
        assertNotNull(replacement.color());
    }

    @Test
    public void templateReplaceRequestBuilderWorks() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .replace("{{surname}}", "Doe")
                .reflowPreset(ReflowPreset.BEST_EFFORT)
                .build();

        assertEquals(2, request.replacements().size());
        assertEquals(ReflowPreset.BEST_EFFORT, request.reflowPreset());
        assertNull(request.pageIndex());
    }

    @Test
    public void templateReplaceRequestWithPageIndex() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{date}}", "2024-01-01")
                .pageIndex(0)
                .build();

        assertEquals(1, request.replacements().size());
        assertEquals(Integer.valueOf(0), request.pageIndex());
    }

    @Test
    public void templateReplaceRequestWithCustomReplacement() {
        Font font = new Font("Times", 11.0);
        TemplateReplacement custom = TemplateReplacement.withFormatting("{{signature}}", "Jane Smith", font, Color.BLACK);

        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .addReplacement(custom)
                .reflowPreset(ReflowPreset.FIT_OR_FAIL)
                .build();

        assertEquals(1, request.replacements().size());
        assertEquals(ReflowPreset.FIT_OR_FAIL, request.reflowPreset());
        assertNotNull(request.replacements().get(0).font());
    }

    @Test
    public void reflowPresetEnumValues() {
        assertEquals(3, ReflowPreset.values().length);
        assertNotNull(ReflowPreset.BEST_EFFORT);
        assertNotNull(ReflowPreset.FIT_OR_FAIL);
        assertNotNull(ReflowPreset.NONE);
    }

    @Test
    public void chainedBuilderWithFont() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .withFont("Helvetica", 12.0)
                .build();

        assertEquals(1, request.replacements().size());
        TemplateReplacement r = request.replacements().get(0);
        assertEquals("{{name}}", r.placeholder());
        assertEquals("John", r.text());
        assertNotNull(r.font());
        assertEquals("Helvetica", r.font().getName());
        assertEquals(12.0, r.font().getSize());
        assertNull(r.color());
    }

    @Test
    public void chainedBuilderWithColor() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{status}}", "ACTIVE")
                .withColor(0, 255, 0)
                .build();

        assertEquals(1, request.replacements().size());
        TemplateReplacement r = request.replacements().get(0);
        assertNull(r.font());
        assertNotNull(r.color());
        assertEquals(0, r.color().getRed());
        assertEquals(255, r.color().getGreen());
        assertEquals(0, r.color().getBlue());
    }

    @Test
    public void chainedBuilderWithFontAndColor() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{header}}", "Title")
                .withFont("Arial", 14.0)
                .withColor(255, 0, 0)
                .build();

        assertEquals(1, request.replacements().size());
        TemplateReplacement r = request.replacements().get(0);
        assertNotNull(r.font());
        assertNotNull(r.color());
        assertEquals("Arial", r.font().getName());
        assertEquals(255, r.color().getRed());
    }

    @Test
    public void chainedBuilderMultipleReplacements() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .withFont("Helvetica", 12.0)
                .replace("{{title}}", "Manager")
                .withColor(0, 0, 255)
                .replace("{{date}}", "2024-01-01")
                .build();

        assertEquals(3, request.replacements().size());

        // First replacement has font
        TemplateReplacement r1 = request.replacements().get(0);
        assertEquals("{{name}}", r1.placeholder());
        assertNotNull(r1.font());
        assertNull(r1.color());

        // Second replacement has color
        TemplateReplacement r2 = request.replacements().get(1);
        assertEquals("{{title}}", r2.placeholder());
        assertNull(r2.font());
        assertNotNull(r2.color());

        // Third replacement has no formatting
        TemplateReplacement r3 = request.replacements().get(2);
        assertEquals("{{date}}", r3.placeholder());
        assertNull(r3.font());
        assertNull(r3.color());
    }

    @Test
    public void chainedBuilderWithReflowPreset() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .withFont("Helvetica", 12.0)
                .reflowPreset(ReflowPreset.BEST_EFFORT)
                .build();

        assertEquals(1, request.replacements().size());
        assertEquals(ReflowPreset.BEST_EFFORT, request.reflowPreset());
    }

    @Test
    public void chainedBuilderWithPageIndex() {
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .pageIndex(0)
                .build();

        assertEquals(1, request.replacements().size());
        assertEquals(Integer.valueOf(0), request.pageIndex());
    }

    @Test
    public void chainedBuilderWithFontObject() {
        Font font = new Font("Times", 16.0);
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .withFont(font)
                .build();

        assertEquals(1, request.replacements().size());
        assertEquals("Times", request.replacements().get(0).font().getName());
    }

    @Test
    public void chainedBuilderWithColorObject() {
        Color color = new Color(100, 150, 200);
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .withColor(color)
                .build();

        assertEquals(1, request.replacements().size());
        assertEquals(100, request.replacements().get(0).color().getRed());
    }

    @Test
    public void templateReplacementWithImage() throws IOException {
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");
        Image image = Image.fromFile(imageFile);
        TemplateReplacement replacement = TemplateReplacement.withImage("{{logo}}", image);

        assertEquals("{{logo}}", replacement.placeholder());
        assertNull(replacement.text());
        assertNull(replacement.font());
        assertNull(replacement.color());
        assertNotNull(replacement.image());
    }

    @Test
    public void templateReplacementRequiresTextOrImage() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TemplateReplacement("{{placeholder}}", null, null, null, null);
        });
    }

    @Test
    public void templateReplaceRequestBuilderWithImage() throws IOException {
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");
        Image image = Image.fromFile(imageFile);

        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replaceWithImage("{{logo}}", image)
                .build();

        assertEquals(1, request.replacements().size());
        TemplateReplacement r = request.replacements().get(0);
        assertEquals("{{logo}}", r.placeholder());
        assertNull(r.text());
        assertNotNull(r.image());
    }

    @Test
    public void mixedTextAndImageReplacements() throws IOException {
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");
        Image image = Image.fromFile(imageFile);

        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("{{name}}", "John")
                .withFont("Helvetica", 12.0)
                .replaceWithImage("{{logo}}", image)
                .build();

        assertEquals(2, request.replacements().size());

        TemplateReplacement textReplacement = request.replacements().get(0);
        assertEquals("{{name}}", textReplacement.placeholder());
        assertEquals("John", textReplacement.text());
        assertNotNull(textReplacement.font());
        assertNull(textReplacement.image());

        TemplateReplacement imageReplacement = request.replacements().get(1);
        assertEquals("{{logo}}", imageReplacement.placeholder());
        assertNull(imageReplacement.text());
        assertNotNull(imageReplacement.image());
    }

    // ===========================
    // E2E Integration Tests
    // ===========================

    @Test
    public void replaceWordWithImage() throws IOException {
        PDFDancer client = createClient();
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");

        // Capture baseline state
        int imageCountBefore = client.page(1).selectImages().size();
        log.debug("Images on page 1 before replacement: {}", imageCountBefore);

        // Use an actual word from the PDF as the placeholder
        List<TextLineReference> linesBefore = client.page(1).selectTextLinesStartingWith("The Complete");
        assertEquals(1, linesBefore.size(), "Expected 'The Complete' text line to exist before replacement");

        // Replace the word with an image
        boolean result = client.replaceWithImage("Complete", imageFile).apply();
        assertTrue(result, "replaceWithImage should succeed");

        // Verify the placeholder text is gone
        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertNumberOfImages(imageCountBefore + 1, 1);

        saveTo(client, "replaceWordWithImage.pdf");
    }

    @Test
    public void replaceWordWithImageExplicitSize() throws IOException {
        PDFDancer client = createClient();
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");

        int imageCountBefore = client.page(1).selectImages().size();
        log.debug("Images on page 1 before replacement: {}", imageCountBefore);

        // Replace with explicit dimensions
        boolean result = client.replaceWithImage("Complete", imageFile, 50, 30).apply();
        assertTrue(result, "replaceWithImage with explicit size should succeed");

        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertNumberOfImages(imageCountBefore + 1, 1);

        saveTo(client, "replaceWordWithImageExplicitSize.pdf");
    }

    @Test
    public void replaceWordWithImageViaFluentBuilder() throws IOException {
        PDFDancer client = createClient();
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");

        int imageCountBefore = client.page(1).selectImages().size();

        // Use the fluent builder to chain a text replacement and an image replacement
        boolean result = client.replace("Obviously", "Clearly")
                .replaceWithImage("Complete", imageFile)
                .apply();
        assertTrue(result, "Mixed text+image replacement should succeed");

        new PDFAssertions(client)
                .assertTextlineExists("Clearly", 1)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertNumberOfImages(imageCountBefore + 1, 1);

        saveTo(client, "replaceWordWithImageFluent.pdf");
    }

    @Test
    public void replaceWordWithImageOnSpecificPage() throws IOException {
        PDFDancer client = createClient();
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");

        int imageCountBefore = client.page(1).selectImages().size();

        // Use the page-scoped API
        boolean result = client.page(1).replaceWithImage("Complete", imageFile).apply();
        assertTrue(result, "Page-scoped replaceWithImage should succeed");

        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertNumberOfImages(imageCountBefore + 1, 1);

        saveTo(client, "replaceWordWithImageOnPage.pdf");
    }

    @Test
    public void replaceWordWithImageViaRequestBuilder() throws IOException {
        PDFDancer client = createClient();
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");
        Image image = Image.fromFile(imageFile);

        int imageCountBefore = client.page(1).selectImages().size();

        // Use the lower-level TemplateReplaceRequest builder
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replaceWithImage("Complete", image)
                .build();

        assertEquals(1, request.replacements().size());
        assertNull(request.replacements().get(0).text());
        assertNotNull(request.replacements().get(0).image());

        boolean result = client.applyReplacements(request);
        assertTrue(result, "applyReplacements with image should succeed");

        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertNumberOfImages(imageCountBefore + 1, 1);

        saveTo(client, "replaceWordWithImageViaRequest.pdf");
    }

    @Test
    public void mixedTextAndImageReplacementE2E() throws IOException {
        PDFDancer client = createClient();
        File imageFile = new File("src/test/resources/fixtures/logo-80.png");
        Image image = Image.fromFile(imageFile);

        int imageCountBefore = client.page(1).selectImages().size();

        // Mix text and image replacements in the same request
        TemplateReplaceRequest request = TemplateReplaceRequest.builder()
                .replace("Obviously", "Clearly")
                .replaceWithImage("Complete", image)
                .build();

        assertEquals(2, request.replacements().size());

        boolean result = client.applyReplacements(request);
        assertTrue(result, "Mixed text+image replacement request should succeed");

        new PDFAssertions(client)
                .assertTextlineExists("Clearly", 1)
                .assertParagraphNotExists("Obviously", 1)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertNumberOfImages(imageCountBefore + 1, 1);

        saveTo(client, "mixedTextAndImageE2E.pdf");
    }
}
