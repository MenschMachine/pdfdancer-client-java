package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextStyleRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesLiteralSelectorWithAllStyleFields() {
        TextStyleRequest request = TextStyleRequest.literal("Important")
                .pages(1, 2)
                .caseSensitive(false)
                .wholeWords(true)
                .maxMatches(3)
                .font("Helvetica-Bold")
                .size(12.5)
                .fillColor(PdfColorRequest.rgb(0.8, 0.1, 0.1).alpha(0.9))
                .strokeColor(PdfColorRequest.gray(0.25))
                .characterSpacing(1.25)
                .wordSpacing(2.5)
                .sourceAnchored()
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Important", json.at("/select/literal").asText());
        assertEquals(1, json.at("/pages/0").asInt());
        assertEquals(2, json.at("/pages/1").asInt());
        assertEquals(false, json.at("/select/caseSensitive").asBoolean());
        assertEquals(true, json.at("/select/wholeWords").asBoolean());
        assertEquals(3, json.at("/select/maxMatches").asInt());
        assertEquals("Helvetica-Bold", json.at("/style/font").asText());
        assertEquals(12.5, json.at("/style/size").asDouble());
        assertEquals("rgb", json.at("/style/fillColor/space").asText());
        assertEquals(0.8, json.at("/style/fillColor/components/0").asDouble());
        assertEquals(0.1, json.at("/style/fillColor/components/1").asDouble());
        assertEquals(0.1, json.at("/style/fillColor/components/2").asDouble());
        assertEquals(0.9, json.at("/style/fillColor/alpha").asDouble());
        assertEquals("gray", json.at("/style/strokeColor/space").asText());
        assertEquals(0.25, json.at("/style/strokeColor/components/0").asDouble());
        assertEquals(1.25, json.at("/style/characterSpacing").asDouble());
        assertEquals(2.5, json.at("/style/wordSpacing").asDouble());
        assertEquals(true, json.at("/style/resetSpacingOverrides").isMissingNode());
        assertEquals("sourceAnchored", json.at("/layout/mode").asText());
    }

    @Test
    void serializesResetSpacingOverridesWithoutSpacingValues() {
        TextStyleRequest request = TextStyleRequest.literal("Important")
                .resetSpacingOverrides(true)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals(true, json.at("/style/resetSpacingOverrides").asBoolean());
        assertEquals(true, json.at("/style/characterSpacing").isMissingNode());
        assertEquals(true, json.at("/style/wordSpacing").isMissingNode());
    }

    @Test
    void serializesRegexSelectorWithCmykAndReflowLayout() {
        TextStyleRequest request = TextStyleRequest.regex("\\bB2B\\b")
                .fillColor(PdfColorRequest.cmyk(0.1, 0.2, 0.3, 0.4))
                .reflowWhenSupported(TextLayoutRequest.Profile.BODY_TEXT)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("\\bB2B\\b", json.at("/select/regex").asText());
        assertEquals("cmyk", json.at("/style/fillColor/space").asText());
        assertEquals(0.1, json.at("/style/fillColor/components/0").asDouble());
        assertEquals(0.2, json.at("/style/fillColor/components/1").asDouble());
        assertEquals(0.3, json.at("/style/fillColor/components/2").asDouble());
        assertEquals(0.4, json.at("/style/fillColor/components/3").asDouble());
        assertEquals("reflowWhenSupported", json.at("/layout/mode").asText());
        assertEquals("bodyText", json.at("/layout/profile").asText());
    }

    @Test
    void serializesRunsWhereSelectorWithSupportedFilters() {
        TextStyleRequest request = TextStyleRequest.runsWhere()
                .whereTextContains("Total")
                .whereFont("Helvetica-Bold")
                .whereSize(12, 0.01)
                .whereFillColor(PdfColorRequest.rgb(0.8, 0.1, 0.1))
                .whereStrokeColor(PdfColorRequest.gray(0.25))
                .whereCharacterSpacing(1.25)
                .whereWordSpacing(2.5, 0.1)
                .whereContainsUnmappedGlyphs(false)
                .maxMatches(100)
                .fillColor(PdfColorRequest.rgb(1, 0, 0))
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Total", json.at("/select/runs/where/textContains").asText());
        assertEquals("Helvetica-Bold", json.at("/select/runs/where/font").asText());
        assertEquals(12.0, json.at("/select/runs/where/size/eq").asDouble());
        assertEquals(0.01, json.at("/select/runs/where/size/tolerance").asDouble());
        assertEquals("rgb", json.at("/select/runs/where/fillColor/space").asText());
        assertEquals(0.8, json.at("/select/runs/where/fillColor/components/0").asDouble());
        assertEquals("gray", json.at("/select/runs/where/strokeColor/space").asText());
        assertEquals(0.25, json.at("/select/runs/where/strokeColor/components/0").asDouble());
        assertEquals(1.25, json.at("/select/runs/where/characterSpacing/eq").asDouble());
        assertEquals(2.5, json.at("/select/runs/where/wordSpacing/eq").asDouble());
        assertEquals(0.1, json.at("/select/runs/where/wordSpacing/tolerance").asDouble());
        assertEquals(false, json.at("/select/runs/where/containsUnmappedGlyphs").asBoolean());
        assertEquals(100, json.at("/select/runs/maxMatches").asInt());
        assertEquals(true, json.at("/select/runs/where/runIds").isMissingNode());
        assertEquals(true, json.at("/select/runs/where/reflowUnitIds").isMissingNode());
        assertEquals(true, json.at("/select/runs/where/elementIdsAny").isMissingNode());
        assertEquals("rgb", json.at("/style/fillColor/space").asText());
        assertEquals(true, json.at("/select/maxMatches").isMissingNode());
    }

    @Test
    void validatesSelectorStyleAndOptions() {
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("").fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.regex(" ").fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").pages(0).fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").maxMatches(0).fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").font(" ").build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").size(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").size(Double.NaN).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").characterSpacing(Double.POSITIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").wordSpacing(Double.NEGATIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").fillColor(null).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").fillColor(PdfColorRequest.rgb(1, 0, 0)).reflowWhenSupported(null).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").characterSpacing(1).resetSpacingOverrides(true).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x").wordSpacing(1).resetSpacingOverrides(true).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.runsWhere().fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.runsWhere().whereTextContains(" ").fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.runsWhere().whereFont(" ").fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.runsWhere().whereSize(Double.NaN).fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.runsWhere().whereSize(12, -0.01).fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.runsWhere().whereTextContains("x").maxMatches(0).fillColor(PdfColorRequest.rgb(1, 0, 0)).build());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleRequest(List.of(1),
                        new TextSelectorRequest("x", "y", null, null, null),
                        new TextStyleRequest.Style(null, 12.0, null, null, null, null, null),
                        null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleRequest(null,
                        new TextSelectorRequest("x", null, null, null, null),
                        new TextStyleRequest.Style(null, 12.0, null, null, null, null, null),
                        new TextLayoutRequest(TextLayoutRequest.Mode.sourceAnchored, TextLayoutRequest.Profile.DEFAULT)).validated());
    }

    @Test
    void validatesColorRequests() {
        assertThrows(IllegalArgumentException.class, () -> PdfColorRequest.rgb(-0.01, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> PdfColorRequest.rgb(1.01, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> PdfColorRequest.rgb(Double.NaN, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> PdfColorRequest.gray(0.5).alpha(1.01));
        assertThrows(IllegalArgumentException.class, () -> new PdfColorRequest(PdfColorRequest.Space.rgb, List.of(1.0, 0.0), null).validated());
        assertThrows(IllegalArgumentException.class, () -> new PdfColorRequest(null, List.of(1.0), null).validated());
        assertThrows(IllegalArgumentException.class, () -> new PdfColorRequest(PdfColorRequest.Space.gray, null, null).validated());
    }

    @Test
    void validatesStylePatchRequests() {
        assertThrows(IllegalArgumentException.class, () -> TextStylePatchRequest.builder().build());
        assertThrows(IllegalArgumentException.class, () -> TextStylePatchRequest.builder().font(" ").build());
        assertThrows(IllegalArgumentException.class, () -> TextStylePatchRequest.builder().size(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextStylePatchRequest.builder().size(Double.NaN).build());
        assertThrows(IllegalArgumentException.class, () -> TextStylePatchRequest.builder().characterSpacing(Double.POSITIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> TextStylePatchRequest.builder().wordSpacing(Double.NEGATIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStylePatchRequest(null, null, null, null, null, null).validated());
    }

    @Test
    void validatesStyleSelectorRequests() {
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleSelectorRequest("x", null, null, null, null,
                        new TextStyleRunsSelectorRequest(
                                new TextStyleRunFilterRequest("y", null, null, null, null, null, null, null),
                                null)).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleSelectorRequest(null, null, null, null, null, null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleRunsSelectorRequest(null, null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextStyleNumericFilterRequest(null, null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                TextStyleNumericFilterRequest.eq(12, Double.POSITIVE_INFINITY));
    }
}
