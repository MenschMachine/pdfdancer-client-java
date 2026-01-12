package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplacement;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // Integration test - requires a PDF with placeholders
    // @Test
    // public void replaceTemplatesInPdf() {
    //     // This would require a test PDF with {{placeholder}} text
    //     PDFDancer pdf = createClient("template-test.pdf");
    //
    //     boolean result = pdf.applyReplacements(
    //             TemplateReplaceRequest.builder()
    //                     .replace("{{name}}", "Test User")
    //                     .build()
    //     );
    //
    //     assertTrue(result);
    // }
}
