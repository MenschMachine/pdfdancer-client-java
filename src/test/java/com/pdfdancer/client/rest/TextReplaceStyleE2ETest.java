package com.pdfdancer.client.rest;

import com.pdfdancer.client.http.HttpRequest;
import com.pdfdancer.client.http.MediaType;
import com.pdfdancer.client.rest.session.SessionService;
import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.response.TextEditResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextReplaceStyleE2ETest extends BaseTest {
    private static final String SOURCE_TEXT = "This line will be replaced.";
    private static final String SPACED_SOURCE_TEXT = "SPACED TOKEN";
    private static final String GLYPH_SOURCE_TEXT = "GLYPH TOKEN";

    @Test
    void allAtomicStyleOverridesPersistInSavedPdf() throws IOException {
        PDFDancer pdf = createStyledTextClient();

        TextEditResponse response = pdf.text().replace(TextReplaceRequest.literal(
                        SOURCE_TEXT, "Atomic styled replacement")
                .font("Helvetica-Bold")
                .size(17)
                .fillColor(PdfColorRequest.rgb(0.1, 0.2, 0.3))
                .strokeColor(PdfColorRequest.rgb(0.4, 0.5, 0.6))
                .characterSpacing(0.25)
                .wordSpacing(1.5)
                .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        List<GlyphStyle> styles = inspectStyles(pdf.getFileBytes(), "Atomic styled replacement");
        assertFalse(styles.isEmpty());
        for (GlyphStyle style : styles) {
            assertEquals("Helvetica-Bold", removeSubsetPrefix(style.fontName()));
            assertEquals(17.0, style.fontSize(), 0.001);
            assertEquals(0x1A334D, style.fillRgb(), style.toString());
            assertEquals(0x668099, style.strokeRgb(), style.toString());
            assertEquals(0.25, style.characterSpacing(), 0.001);
            assertEquals(1.5, style.wordSpacing(), 0.001);
        }
    }

    @Test
    void resetSpacingOverridesClearsInheritedSpacing() throws IOException {
        PDFDancer pdf = createStyledTextClient();

        TextEditResponse response = pdf.text().replace(TextReplaceRequest.literal(
                        SPACED_SOURCE_TEXT, "Neutral replacement spacing")
                .resetSpacingOverrides()
                .build());

        assertEquals(1, response.matched());
        assertEquals(1, response.changed(), response.toString());

        List<GlyphStyle> styles = inspectStyles(pdf.getFileBytes(), "Neutral replacement spacing");
        assertFalse(styles.isEmpty());
        for (GlyphStyle style : styles) {
            assertEquals(0.0, style.characterSpacing(), 0.001);
            assertEquals(0.0, style.wordSpacing(), 0.001);
        }
    }

    @Test
    void unsupportedGlyphDoesNotMutateDocument() throws IOException {
        PDFDancer pdf = createStyledTextClient();

        TextEditResponse response = pdf.text().replace(TextReplaceRequest.literal(GLYPH_SOURCE_TEXT, "漢")
                .font("Helvetica")
                .build());

        assertEquals(1, response.matched());
        assertEquals(0, response.changed());
        assertEquals(1, response.errors().size());
        assertEquals("TEXT_FONT_GLYPH_UNSUPPORTED", response.errors().get(0).code());
        String savedText = extractText(pdf.getFileBytes());
        assertTrue(savedText.contains(GLYPH_SOURCE_TEXT));
        assertFalse(savedText.contains("漢"));
    }

    @Test
    void serverRejectsEveryInvalidReplacementStyleWithoutMutation() throws IOException {
        byte[] sourcePdf = createStyledTextPdf();
        String token = getValidToken();
        String sessionId = SessionService.uploadPdfForSession(token, sourcePdf, httpClient);

        List<InvalidRequestCase> cases = List.of(
                new InvalidRequestCase("empty style", textReplacement(Map.of()),
                        "style must contain at least one field"),
                new InvalidRequestCase("style with image", imageReplacement(Map.of("font", "Helvetica")),
                        "style is not valid with replaceWithImage"),
                new InvalidRequestCase("false reset", textReplacement(Map.of("resetSpacingOverrides", false)),
                        "style.resetSpacingOverrides must be true when present"),
                new InvalidRequestCase("reset with spacing", textReplacement(Map.of(
                        "resetSpacingOverrides", true,
                        "characterSpacing", 0.25)),
                        "style.resetSpacingOverrides cannot be combined with characterSpacing or wordSpacing"),
                new InvalidRequestCase("blank font", textReplacement(Map.of("font", " ")),
                        "style.font must not be blank"),
                new InvalidRequestCase("unknown font", textReplacement(Map.of("font", "No-Such-Font")),
                        "style.font font not found: No-Such-Font"),
                new InvalidRequestCase("invalid size", textReplacement(Map.of("size", 0)),
                        "style.size must be positive and finite"),
                new InvalidRequestCase("invalid fill color", textReplacement(Map.of(
                        "fillColor", Map.of("space", "rgb", "components", List.of(2, 0, 0)))),
                        "style.fillColor.components values must be finite numbers from 0.0 to 1.0")
        );

        for (InvalidRequestCase invalid : cases) {
            PdfDancerClientException error = assertThrows(PdfDancerClientException.class, () ->
                    httpClient.toBlocking().retrieve(
                            HttpRequest.POST("/pdf/text/replace", invalid.body())
                                    .contentType(MediaType.APPLICATION_JSON_TYPE)
                                    .bearerAuth(token)
                                    .header("X-Session-Id", sessionId),
                            TextEditResponse.class), invalid.name());
            assertEquals(400, error.getStatusCode(), invalid.name());
            assertTrue(error.getMessage().contains(invalid.expectedMessage()),
                    invalid.name() + ": " + error.getMessage());
        }

        byte[] savedPdf = httpClient.toBlocking().retrieve(
                HttpRequest.GET("/session/" + sessionId + "/pdf").bearerAuth(token),
                byte[].class);
        assertTrue(extractText(savedPdf).contains(SOURCE_TEXT));
    }

    private static Map<String, Object> textReplacement(Map<String, Object> style) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("select", Map.of("literal", SOURCE_TEXT));
        body.put("replaceWith", "replacement");
        body.put("style", style);
        return body;
    }

    private static Map<String, Object> imageReplacement(Map<String, Object> style) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("select", Map.of("literal", SOURCE_TEXT));
        body.put("replaceWithImage", Map.of(
                "data", new byte[]{1},
                "transformationMatrix", List.of(1, 0, 0, 1, 0, 0)));
        body.put("style", style);
        return body;
    }

    private static PDFDancer createStyledTextClient() throws IOException {
        return PDFDancer.createSession(getValidToken(), createStyledTextPdf(), httpClient);
    }

    private static byte[] createStyledTextPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 720);
                content.showText(SOURCE_TEXT);
                content.endText();

                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.setCharacterSpacing(2);
                content.setWordSpacing(3);
                content.newLineAtOffset(72, 680);
                content.showText(SPACED_SOURCE_TEXT);
                content.endText();

                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.setCharacterSpacing(0);
                content.setWordSpacing(0);
                content.newLineAtOffset(72, 640);
                content.showText(GLYPH_SOURCE_TEXT);
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private static List<GlyphStyle> inspectStyles(byte[] pdfBytes, String text) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            TextStyleCollector collector = new TextStyleCollector();
            collector.getText(document);
            return collector.stylesFor(text);
        }
    }

    private static String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String removeSubsetPrefix(String fontName) {
        return fontName != null && fontName.matches("^[A-Z]{6}\\+.+")
                ? fontName.substring(7)
                : fontName;
    }

    private record InvalidRequestCase(
            String name,
            Map<String, Object> body,
            String expectedMessage) {
    }

    private record GlyphStyle(
            String fontName,
            double fontSize,
            int fillRgb,
            int strokeRgb,
            double characterSpacing,
            double wordSpacing) {
    }

    private static final class TextStyleCollector extends PDFTextStripper {
        private final StringBuilder text = new StringBuilder();
        private final List<GlyphStyle> styles = new ArrayList<>();

        private TextStyleCollector() throws IOException {
            addOperator(new SetNonStrokingDeviceCMYKColor(this));
            addOperator(new SetNonStrokingDeviceGrayColor(this));
            addOperator(new SetNonStrokingDeviceRGBColor(this));
            addOperator(new SetStrokingDeviceCMYKColor(this));
            addOperator(new SetStrokingDeviceGrayColor(this));
            addOperator(new SetStrokingDeviceRGBColor(this));
        }

        @Override
        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
            String shownText = switch (operator.getName()) {
                case "Tj", "'", "\"" -> decode((COSString) operands.get(operands.size() - 1));
                case "TJ" -> decode((COSArray) operands.get(0));
                default -> null;
            };
            if (shownText != null) {
                var graphicsState = getGraphicsState();
                var textState = graphicsState.getTextState();
                GlyphStyle style = new GlyphStyle(
                        textState.getFont().getName(),
                        textState.getFontSize(),
                        graphicsState.getNonStrokingColor().toRGB(),
                        graphicsState.getStrokingColor().toRGB(),
                        textState.getCharacterSpacing(),
                        textState.getWordSpacing());
                text.append(shownText);
                for (int i = 0; i < shownText.length(); i++) {
                    styles.add(style);
                }
            }
            super.processOperator(operator, operands);
        }

        private String decode(COSArray array) throws IOException {
            StringBuilder decoded = new StringBuilder();
            for (COSBase item : array) {
                if (item instanceof COSString string) {
                    decoded.append(decode(string));
                }
            }
            return decoded.toString();
        }

        private String decode(COSString string) throws IOException {
            PDFont font = getGraphicsState().getTextState().getFont();
            StringBuilder decoded = new StringBuilder();
            try (ByteArrayInputStream input = new ByteArrayInputStream(string.getBytes())) {
                while (input.available() > 0) {
                    int code = font.readCode(input);
                    String unicode = font.toUnicode(code);
                    if (unicode != null) {
                        decoded.append(unicode);
                    }
                }
            }
            return decoded.toString();
        }

        private List<GlyphStyle> stylesFor(String expectedText) {
            int start = text.indexOf(expectedText);
            assertTrue(start >= 0,
                    () -> "Expected saved PDF text to contain '" + expectedText + "' but was: " + text);
            return List.copyOf(styles.subList(start, start + expectedText.length()));
        }
    }
}
