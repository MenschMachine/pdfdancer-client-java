package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PdfAffineTransform;
import com.pdfdancer.common.model.PositionBuilder;
import com.pdfdancer.common.model.Size;
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyntheticTextReplacementE2ETest extends BaseTest {
    private static final String FIXTURE_DIR = "src/test/resources/fixtures/";
    private static final String SYNTHETIC_FIXTURE =
            "synthetic-text/SyntheticTextReplacement.pdf";
    private static final File IMAGE_FIXTURE = new File(FIXTURE_DIR + "logo-80.png");
    private static final int PAGE_NUMBER = 1;

    @Test
    void syntheticTextAndImagePlaceholdersCanBeReplacedWithCurrentTextApi() throws IOException {
        PDFDancer pdf = createClient(SYNTHETIC_FIXTURE);
        int originalImageCount = pdf.page(PAGE_NUMBER).selectImages().size();
        assertEquals(0, originalImageCount, "Synthetic source PDF must not contain images");
        registerSyntheticFonts(pdf);

        List<ReplacementCase> replacements = replacements();
        for (ReplacementCase replacement : replacements) {
            TextEditResponse response = pdf.text().replace(TextReplaceRequest.literal(
                            replacement.originalText(),
                            replacement.replacementText())
                    .layout(replacement.layout())
                    .build());

            assertEquals(1, response.matched(), "Expected one match for " + replacement.originalText());
            assertEquals(1, response.changed(), "Expected one change for " + replacement.originalText());
            assertFalse(response.change().isEmpty(),
                    "Expected change diagnostic for " + replacement.originalText());
            assertEquals(replacement.replacementText(), response.change().get(0).resultText());
            assertEquals(
                    replacement.layout().mode().name(),
                    response.change().get(0).requestedLayoutMode());
            assertEquals(
                    replacement.layout().profile() == null
                            ? null
                            : replacement.layout().profile().value(),
                    response.change().get(0).requestedLayoutProfile());
            assertEquals(
                    replacement.expectedEffectiveHyphenationEnabled(),
                    response.change().get(0).effectiveHyphenationEnabled());
            if (replacement.layout().mode() == TextLayoutRequest.Mode.requireReflow) {
                assertEquals("REFLOWED", response.change().get(0).appliedLayoutMode());
                assertTrue(response.errors().isEmpty(),
                        "Required reflow must not report errors: " + response.errors());
                assertFalse(response.warnings().stream()
                                .anyMatch(warning -> "REFLOW_FALLBACK".equals(warning.code())),
                        "Required reflow must not fall back: " + response.warnings().stream()
                                .map(warning -> warning.code() + ": " + warning.message())
                                .toList());
            }
        }

        replaceBrandMark(pdf);
        replaceAccessBadge(pdf);

        PDFAssertions assertions = new PDFAssertions(pdf);
        for (ReplacementCase replacement : replacements) {
            assertions.assertPdfTextDoesNotContain(replacement.originalText());
            assertions.assertPdfTextUsesFont(
                    replacement.fontAssertionText(),
                    replacement.expectedFontName(),
                    PAGE_NUMBER);
        }
        assertions
                .assertPdfTextDoesNotContain("{{brand_mark}}")
                .assertPdfTextDoesNotContain("{{access_badge}}")
                .assertNumberOfImages(originalImageCount + 3, PAGE_NUMBER);
    }

    private static void replaceBrandMark(PDFDancer pdf) throws IOException {
        List<ImageReference> imagesBefore = pdf.page(PAGE_NUMBER).selectImages();
        TextEditResponse response = pdf.text().replace(TextReplaceRequest.builder()
                .literal("{{brand_mark}}")
                .replaceWithImage(
                        IMAGE_FIXTURE,
                        PdfAffineTransform.builder()
                                .scale(64, 24)
                                .build())
                .build());

        ImageReference brandMark = generatedImage(pdf, response, "{{brand_mark}}");
        assertEquals(64.0, brandMark.getWidth(), 1.0, "Replacement brand-mark width");
        assertEquals(24.0, brandMark.getHeight(), 1.0, "Replacement brand-mark height");
        assertEquals(imagesBefore.size() + 1, pdf.page(PAGE_NUMBER).selectImages().size());

        addCompanionMark(pdf, brandMark);
        assertEquals(imagesBefore.size() + 2, pdf.page(PAGE_NUMBER).selectImages().size());
    }

    private static void addCompanionMark(PDFDancer pdf, ImageReference replacementBrandMark)
            throws IOException {
        Image companionMark = Image.fromFile(IMAGE_FIXTURE);
        Size originalSize = companionMark.getSize();
        double width = originalSize != null && originalSize.getHeight() > 0
                ? originalSize.getWidth() * 24 / originalSize.getHeight()
                : 24;
        companionMark.setSize(new Size(width, 24));

        Double markX = replacementBrandMark.getPosition().getX();
        Double markY = replacementBrandMark.getPosition().getY();
        Double markWidth = replacementBrandMark.getWidth();
        assertNotNull(markX, "Replacement brand mark has no x coordinate");
        assertNotNull(markY, "Replacement brand mark has no y coordinate");
        assertNotNull(markWidth, "Replacement brand mark has no width");

        assertTrue(pdf.addImage(
                        companionMark,
                        new PositionBuilder()
                                .onPage(PAGE_NUMBER)
                                .atCoordinates(markX + markWidth + 12.0, markY)
                                .build()),
                "Could not add companion brand mark");
    }

    private static void replaceAccessBadge(PDFDancer pdf) throws IOException {
        List<ImageReference> imagesBefore = pdf.page(PAGE_NUMBER).selectImages();
        TextEditResponse response = pdf.text().replace(TextReplaceRequest.builder()
                .literal("{{access_badge}}")
                .replaceWithImage(
                        IMAGE_FIXTURE,
                        PdfAffineTransform.builder()
                                .scale(42, 42)
                                .translate(8, -24)
                                .build())
                .build());

        ImageReference accessBadge = generatedImage(pdf, response, "{{access_badge}}");
        assertEquals(42.0, accessBadge.getWidth(), 1.0, "Replacement access-badge width");
        assertEquals(42.0, accessBadge.getHeight(), 1.0, "Replacement access-badge height");
        assertEquals(imagesBefore.size() + 1, pdf.page(PAGE_NUMBER).selectImages().size());
    }

    private static ImageReference generatedImage(
            PDFDancer pdf,
            TextEditResponse response,
            String placeholder) {
        assertEquals(1, response.matched(), "Expected one match for " + placeholder);
        assertEquals(1, response.changed(), "Expected one image replacement for " + placeholder);
        assertEquals(1, response.change().size(), "Expected one change diagnostic for " + placeholder);
        assertEquals("replaceWithImage", response.change().get(0).operation());
        assertNotNull(response.change().get(0).generatedElementIds());
        assertEquals(1, response.change().get(0).generatedElementIds().size());

        String generatedImageId = response.change().get(0).generatedElementIds().get(0);
        return pdf.page(PAGE_NUMBER).selectImages().stream()
                .filter(image -> generatedImageId.equals(image.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Could not locate generated image " + generatedImageId + " for " + placeholder));
    }

    private static void registerSyntheticFonts(PDFDancer pdf) {
        for (String fontName : List.of(
                "Roboto-Regular.ttf",
                "SourceSans3-Regular.ttf",
                "JetBrainsMono-Regular.ttf",
                "DancingScript-Regular.ttf")) {
            pdf.registerFont(new File(FIXTURE_DIR + fontName));
        }
    }

    private static List<ReplacementCase> replacements() {
        return List.of(
                new ReplacementCase(
                        "{{organization_name}} employee",
                        "Blue River Analytics employee",
                        "Blue River Analytics employee",
                        "Roboto-Regular",
                        TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                                .withHyphenationEnabled(true),
                        true),
                new ReplacementCase(
                        "{{organization_name}} members use the synthetic program",
                        "Blue River Analytics members use the expanded synthetic benefits and support program",
                        "expanded",
                        "SourceSans3-Regular",
                        TextLayoutRequest.requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                                .withHyphenationEnabled(false),
                        false),
                new ReplacementCase(
                        "{{support_phone}}",
                        "+1 202 555 0147",
                        "+1 202 555 0147",
                        "JetBrainsMono-Regular",
                        TextLayoutRequest.sourceAnchored(),
                        false),
                new ReplacementCase(
                        "{{campaign_code}}",
                        "ORBIT-7",
                        "ORBIT-7",
                        "DancingScript-Regular",
                        TextLayoutRequest.sourceAnchored(),
                        false));
    }

    private record ReplacementCase(
            String originalText,
            String replacementText,
            String fontAssertionText,
            String expectedFontName,
            TextLayoutRequest layout,
            boolean expectedEffectiveHyphenationEnabled) {
    }
}
