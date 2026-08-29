package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.response.CommandResult;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FocusLeseprobeE2ETest extends BaseTest {
    private static final String FOCUS_FIXTURE =
            "magazines/FOCUS_2026-34_Leseprobe-page2-3.pdf";
    private static final Path REPLACEMENT_IMAGE =
            Path.of("src/test/resources/fixtures/images/rhein-trocken.jpeg");
    private static final double MAIN_IMAGE_X = 425.0;
    private static final double MAIN_IMAGE_Y = 150.0;
    private static final String SOURCE_TEXT_REGEX =
            "Ilse\\s+–\\s+wer\\s+will\\s+sie(?s:.*?)Aigner\\s+den\\s+Sprung\\?";
    private static final String BLUE_TEXT_REGEX = "^A$";
    private static final String REPLACEMENT_TEXT = "Bodo – ist er wieder da? Sieben rote Boote fahren heute über den stillen Hafen. Danach zählen Gäste sieben kleine Kisten: Beginnt morgen die Reise? Wohin wird es gehen?";

    @Override
    protected String getPdfFile() {
        return FOCUS_FIXTURE;
    }

    @Test
    void replacesFirstPageImageWithRheinTrocken() throws IOException {
        PDFDancer pdf = createClient();

        TextEditResponse textEdit = pdf.page(1).text().replace(
                TextReplaceRequest.regex(SOURCE_TEXT_REGEX, REPLACEMENT_TEXT)
                        .maxMatches(1)
                        .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .build());
        assertEquals(1, textEdit.matched(), textEdit.toString());
        assertEquals(1, textEdit.changed(), textEdit.toString());
        assertNotNull(textEdit.change(), textEdit.toString());
        assertFalse(textEdit.change().isEmpty(), textEdit.toString());
        assertEquals(REPLACEMENT_TEXT, textEdit.change().get(0).resultText());

        TextEditResponse contactColorEdit = pdf.page(1).text().style(
                TextStyleRequest.regex(BLUE_TEXT_REGEX)
                        .maxMatches(1)
                        .fillColor(PdfColorRequest.rgb(0.0, 0.0, 1.0))
                        .build());
        assertEquals(1, contactColorEdit.matched(), contactColorEdit.toString());
        assertEquals(1, contactColorEdit.changed(), contactColorEdit.toString());

        List<ImageReference> matchingImages = pdf.page(1)
                .selectImagesAt(MAIN_IMAGE_X, MAIN_IMAGE_Y);
        assertEquals(1, matchingImages.size(), matchingImages.toString());

        ImageReference image = matchingImages.get(0);
        assertNotNull(image.getWidth(), image.toString());
        assertNotNull(image.getHeight(), image.toString());
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        Image replacement = Image.fromFile(REPLACEMENT_IMAGE.toFile());
        replacement.setSize(null); // TODO make it explicit with ImagePlacement.preserveDisplayBox()
        CommandResult imageEdit = image.replace(replacement);
        assertTrue(imageEdit.success(), imageEdit.toString());

        new PDFAssertions(pdf)
                .assertPdfTextContains("Bodo", 1)
                .assertPdfTextContains("Sieben", 1)
                .assertPdfTextContains("Reise?", 1)
                .assertPdfTextDoesNotContain("Ilse", 1)
                .assertImageAt(MAIN_IMAGE_X, MAIN_IMAGE_Y, 1)
                .assertImageSize(MAIN_IMAGE_X, MAIN_IMAGE_Y, 1, originalWidth, originalHeight, 0.01);
    }
}
