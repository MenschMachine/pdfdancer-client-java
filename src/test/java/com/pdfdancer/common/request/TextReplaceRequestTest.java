package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PdfAffineTransform;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextReplaceRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesLiteralSelector() throws Exception {
        TextReplaceRequest request = TextReplaceRequest.literal("Acme", "Globex")
                .pages(1, 2)
                .caseSensitive(false)
                .wholeWords(true)
                .maxMatches(3)
                .sourceAnchored()
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Acme", json.at("/select/literal").asText());
        assertEquals("Globex", json.at("/replaceWith").asText());
        assertEquals(1, json.at("/pages/0").asInt());
        assertEquals(2, json.at("/pages/1").asInt());
        assertEquals(false, json.at("/select/caseSensitive").asBoolean());
        assertEquals(true, json.at("/select/wholeWords").asBoolean());
        assertEquals(3, json.at("/select/maxMatches").asInt());
        assertEquals("sourceAnchored", json.at("/layout/mode").asText());
    }

    @Test
    void serializesRegexSelectorWithReflowLayout() {
        TextReplaceRequest request = TextReplaceRequest.regex("\\bINV-[0-9]{6}\\b", "invoice")
                .reflowWhenSupported(TextLayoutRequest.Profile.BODY_TEXT)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("\\bINV-[0-9]{6}\\b", json.at("/select/regex").asText());
        assertEquals("reflowWhenSupported", json.at("/layout/mode").asText());
        assertEquals("bodyText", json.at("/layout/profile").asText());
    }

    @Test
    void serializesRequireReflowLayout() {
        TextReplaceRequest request = TextReplaceRequest.literal("Acme", "Globex")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("requireReflow", json.at("/layout/mode").asText());
        assertEquals("bodyText", json.at("/layout/profile").asText());
    }

    @Test
    void serializesAndDeserializesEveryAtomicStyleOverride() throws Exception {
        TextReplaceRequest request = TextReplaceRequest.literal("token", "replacement")
                .font("Helvetica-Bold")
                .size(17)
                .fillColor(PdfColorRequest.rgb(0.1, 0.2, 0.3))
                .strokeColor(PdfColorRequest.gray(0.4))
                .characterSpacing(0.25)
                .wordSpacing(1.5)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Helvetica-Bold", json.at("/style/font").asText());
        assertEquals(17.0, json.at("/style/size").asDouble());
        assertEquals("rgb", json.at("/style/fillColor/space").asText());
        assertEquals(0.2, json.at("/style/fillColor/components/1").asDouble());
        assertEquals("gray", json.at("/style/strokeColor/space").asText());
        assertEquals(0.25, json.at("/style/characterSpacing").asDouble());
        assertEquals(1.5, json.at("/style/wordSpacing").asDouble());

        TextReplaceRequest decoded = mapper.treeToValue(json, TextReplaceRequest.class).validated();
        assertEquals(request, decoded);
    }

    @Test
    void serializesResetSpacingOverrides() {
        TextReplaceRequest request = TextReplaceRequest.literal("token", "replacement")
                .resetSpacingOverrides()
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals(true, json.at("/style/resetSpacingOverrides").asBoolean());
    }

    @Test
    void fluentStyleMethodsMergeAndLaterStyleReplacesAccumulatedStyle() {
        TextStyleSetRequest supplied = TextStyleSetRequest.builder()
                .font("Helvetica")
                .size(11)
                .build();

        TextReplaceRequest merged = TextReplaceRequest.literal("token", "replacement")
                .style(supplied)
                .font("Helvetica-Bold")
                .build();
        assertEquals("Helvetica-Bold", merged.style().font());
        assertEquals(11.0, merged.style().size());

        TextReplaceRequest replaced = TextReplaceRequest.literal("token", "replacement")
                .font("Courier")
                .style(supplied)
                .build();
        assertEquals(supplied, replaced.style());
    }

    @Test
    void unstyledReplacementOmitsStyle() {
        TextReplaceRequest request = TextReplaceRequest.literal("token", "replacement").build();

        assertNull(request.style());
        assertEquals(true, mapper.valueToTree(request).at("/style").isMissingNode());
    }

    @Test
    void allowsEmptyReplacement() {
        TextReplaceRequest request = TextReplaceRequest.literal("delete me", "")
                .build();

        assertEquals("", request.replaceWith());
    }

    @Test
    void serializesImageReplacementUsingBase64AndPdfMatrixOrder() {
        Image image = new Image();
        image.setData(new byte[]{1, 2, 3});
        PdfAffineTransform transformation = PdfAffineTransform.builder()
                .scale(20, 10)
                .translate(3, -2)
                .build();

        TextReplaceRequest request = TextReplaceRequest.builder()
                .literal("{{logo}}")
                .replaceWithImage(image, transformation)
                .sourceAnchored()
                .build();
        JsonNode json = mapper.valueToTree(request);

        assertEquals("AQID", json.at("/replaceWithImage/data").asText());
        assertEquals(20.0, json.at("/replaceWithImage/transformationMatrix/0").asDouble());
        assertEquals(0.0, json.at("/replaceWithImage/transformationMatrix/1").asDouble());
        assertEquals(0.0, json.at("/replaceWithImage/transformationMatrix/2").asDouble());
        assertEquals(10.0, json.at("/replaceWithImage/transformationMatrix/3").asDouble());
        assertEquals(3.0, json.at("/replaceWithImage/transformationMatrix/4").asDouble());
        assertEquals(-2.0, json.at("/replaceWithImage/transformationMatrix/5").asDouble());
        assertEquals(true, json.at("/replaceWith").isMissingNode());
    }

    @Test
    void imageReplacementJsonRoundTripsToDomainTransform() throws Exception {
        String json = """
                {
                  "select": {"literal": "{{logo}}"},
                  "replaceWithImage": {
                    "data": "AQID",
                    "transformationMatrix": [20, 0, 5, 10, 3, -2]
                  },
                  "layout": {"mode": "sourceAnchored"}
                }
                """;

        TextReplaceRequest request = mapper.readValue(json, TextReplaceRequest.class).validated();

        assertEquals(PdfAffineTransform.fromPdfMatrix(new double[]{20, 0, 5, 10, 3, -2}),
                request.replaceWithImage().transformation());
        assertEquals(3, request.replaceWithImage().data().length);
    }

    @Test
    void imageReplacementValidationMatchesApiContract() {
        TextSelectorRequest selector = new TextSelectorRequest("token", null, null, null, null);
        PdfAffineTransform identity = PdfAffineTransform.builder().build();

        assertThrows(IllegalArgumentException.class, () -> new TextReplaceRequest(
                null, selector, "text", new TextReplacementImageRequest(new byte[]{1}, identity), null).validated());
        assertThrows(IllegalArgumentException.class, () -> new TextReplaceRequest(
                null, selector, null, new TextReplacementImageRequest(new byte[0], identity), null).validated());
        assertThrows(IllegalArgumentException.class, () -> new TextReplaceRequest(
                null, selector, null, new TextReplacementImageRequest(new byte[]{1}, null), null).validated());

        Image image = new Image();
        image.setData(new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.builder()
                .literal("token")
                .replaceWithImage(image, identity)
                .build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.builder()
                .literal("token")
                .replaceWithImage(image, identity)
                .reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                .build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.builder()
                .literal("token")
                .replaceWithImage(image, identity)
                .font("Helvetica")
                .sourceAnchored()
                .build());
    }

    @Test
    void validatesAtomicStyleOverrides() {
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleSetRequest(null, null, null, null, null, null, null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleSetRequest(null, null, null, null, null, null, false).validated());
        assertThrows(IllegalArgumentException.class, () -> TextStyleSetRequest.builder().font(" ").build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleSetRequest.builder().size(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleSetRequest.builder().size(Double.NaN).build());
        assertThrows(IllegalArgumentException.class, () ->
                TextStyleSetRequest.builder().characterSpacing(Double.POSITIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () ->
                TextStyleSetRequest.builder().wordSpacing(Double.NEGATIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleSetRequest.builder()
                .characterSpacing(0.25)
                .resetSpacingOverrides()
                .build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleSetRequest.builder()
                .wordSpacing(1.5)
                .resetSpacingOverrides()
                .build());
    }

    @Test
    void validatesSelectorAndOptions() {
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("", "x").build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.regex(" ", "x").build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("x", null).build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("x", "y").pages(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("x", "y").maxMatches(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("x", "y").reflowWhenSupported(null).build());
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("x", "y").requireReflow(null).build());
        assertThrows(IllegalArgumentException.class, () ->
                new TextReplaceRequest(List.of(1),
                        new TextSelectorRequest("x", "y", null, null, null),
                        "z",
                        null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextReplaceRequest(null,
                        new TextSelectorRequest("x", null, null, null, null),
                        "z",
                        new TextLayoutRequest(TextLayoutRequest.Mode.sourceAnchored, TextLayoutRequest.Profile.DEFAULT)).validated());
    }
}
