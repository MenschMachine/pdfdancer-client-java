package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextInsertRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesLiteralAfterAnchor() {
        TextInsertRequest request = TextInsertRequest.after("Assumptions", " Overview")
                .pages(1, 2)
                .caseSensitive(false)
                .wholeWords(true)
                .maxMatches(3)
                .sourceAnchored()
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Assumptions", json.at("/target/anchor/select/literal").asText());
        assertEquals("after", json.at("/target/anchor/caret").asText());
        assertEquals(1, json.at("/target/anchor/pages/0").asInt());
        assertEquals(2, json.at("/target/anchor/pages/1").asInt());
        assertEquals(false, json.at("/target/anchor/select/caseSensitive").asBoolean());
        assertEquals(true, json.at("/target/anchor/select/wholeWords").asBoolean());
        assertEquals(3, json.at("/target/anchor/select/maxMatches").asInt());
        assertEquals(" Overview", json.at("/insert").asText());
        assertEquals("anchor", json.at("/style/from").asText());
        assertEquals(true, json.at("/style/patch").isMissingNode());
        assertEquals("sourceAnchored", json.at("/layout/mode").asText());
    }

    @Test
    void serializesLiteralBeforeAnchor() {
        TextInsertRequest request = TextInsertRequest.before("Assumptions", "Context: ").build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Assumptions", json.at("/target/anchor/select/literal").asText());
        assertEquals("before", json.at("/target/anchor/caret").asText());
        assertEquals("Context: ", json.at("/insert").asText());
    }

    @Test
    void serializesRegexBeforeAndAfterAnchorWithReflowLayout() {
        TextInsertRequest before = TextInsertRequest.beforeRegex("\\bINV-[0-9]{6}\\b", "Invoice ")
                .reflowWhenSupported(TextLayoutRequest.Profile.BODY_TEXT)
                .build();
        TextInsertRequest after = TextInsertRequest.afterRegex("\\bINV-[0-9]{6}\\b", " paid")
                .reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                .build();

        JsonNode beforeJson = mapper.valueToTree(before);
        JsonNode afterJson = mapper.valueToTree(after);

        assertEquals("\\bINV-[0-9]{6}\\b", beforeJson.at("/target/anchor/select/regex").asText());
        assertEquals("before", beforeJson.at("/target/anchor/caret").asText());
        assertEquals("reflowWhenSupported", beforeJson.at("/layout/mode").asText());
        assertEquals("bodyText", beforeJson.at("/layout/profile").asText());
        assertEquals("after", afterJson.at("/target/anchor/caret").asText());
        assertEquals("noReflow", afterJson.at("/layout/profile").asText());
    }

    @Test
    void serializesStylePatchFromBuilderConvenienceMethods() {
        TextInsertRequest request = TextInsertRequest.after("Assumptions", " Overview")
                .font("Helvetica-Bold")
                .size(12.5)
                .fillColor(PdfColorRequest.rgb(0.8, 0.1, 0.1).alpha(0.9))
                .strokeColor(PdfColorRequest.gray(0.25))
                .characterSpacing(1.25)
                .wordSpacing(2.5)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("anchor", json.at("/style/from").asText());
        assertEquals("Helvetica-Bold", json.at("/style/patch/font").asText());
        assertEquals(12.5, json.at("/style/patch/size").asDouble());
        assertEquals("rgb", json.at("/style/patch/fillColor/space").asText());
        assertEquals(0.8, json.at("/style/patch/fillColor/components/0").asDouble());
        assertEquals(0.1, json.at("/style/patch/fillColor/components/1").asDouble());
        assertEquals(0.1, json.at("/style/patch/fillColor/components/2").asDouble());
        assertEquals(0.9, json.at("/style/patch/fillColor/alpha").asDouble());
        assertEquals("gray", json.at("/style/patch/strokeColor/space").asText());
        assertEquals(0.25, json.at("/style/patch/strokeColor/components/0").asDouble());
        assertEquals(1.25, json.at("/style/patch/characterSpacing").asDouble());
        assertEquals(2.5, json.at("/style/patch/wordSpacing").asDouble());
        assertEquals(true, json.at("/style/patch/resetSpacingOverrides").isMissingNode());
    }

    @Test
    void serializesExplicitStylePatch() {
        TextStylePatchRequest patch = TextStylePatchRequest.builder()
                .size(13)
                .fillColor(PdfColorRequest.cmyk(0.1, 0.2, 0.3, 0.4))
                .build();

        TextInsertRequest request = TextInsertRequest.before("Assumptions", "Context: ")
                .stylePatch(patch)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals(13.0, json.at("/style/patch/size").asDouble());
        assertEquals("cmyk", json.at("/style/patch/fillColor/space").asText());
        assertEquals(0.1, json.at("/style/patch/fillColor/components/0").asDouble());
        assertEquals(0.2, json.at("/style/patch/fillColor/components/1").asDouble());
        assertEquals(0.3, json.at("/style/patch/fillColor/components/2").asDouble());
        assertEquals(0.4, json.at("/style/patch/fillColor/components/3").asDouble());
    }

    @Test
    void serializesCoordinateTargetWithCompleteStylePatch() {
        TextInsertRequest request = TextInsertRequest.at(1, 72, 144, "Coordinate Text")
                .rotationDegrees(90)
                .font("Helvetica-Bold")
                .size(12)
                .fillColor(PdfColorRequest.rgb(1, 0, 0))
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals(1, json.at("/target/coordinate/page").asInt());
        assertEquals(72.0, json.at("/target/coordinate/x").asDouble());
        assertEquals(144.0, json.at("/target/coordinate/y").asDouble());
        assertEquals(90.0, json.at("/target/coordinate/rotationDegrees").asDouble());
        assertEquals(true, json.at("/target/anchor").isMissingNode());
        assertEquals("Coordinate Text", json.at("/insert").asText());
        assertEquals(true, json.at("/style/from").isMissingNode());
        assertEquals("Helvetica-Bold", json.at("/style/patch/font").asText());
        assertEquals(12.0, json.at("/style/patch/size").asDouble());
        assertEquals("rgb", json.at("/style/patch/fillColor/space").asText());
    }

    @Test
    void serializesPageScopedCoordinateTargetWithoutInitialPage() {
        TextInsertRequest request = TextInsertRequest.builder()
                .coordinate(72, 144)
                .insert("Page scoped")
                .font("Helvetica")
                .size(10)
                .build()
                .withPages(List.of(3));

        JsonNode json = mapper.valueToTree(request.validated());

        assertEquals(3, json.at("/target/coordinate/page").asInt());
        assertEquals(72.0, json.at("/target/coordinate/x").asDouble());
        assertEquals(144.0, json.at("/target/coordinate/y").asDouble());
        assertEquals(true, json.at("/style/from").isMissingNode());
    }

    @Test
    void allowsWhitespaceOnlyInsert() {
        TextInsertRequest request = TextInsertRequest.after("x", " ").build();

        assertEquals(" ", request.insert());
    }

    @Test
    void validatesTargetInsertAndOptions() {
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("", "x").build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.afterRegex(" ", "x").build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "").build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", null).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").pages(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").maxMatches(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").caret(null).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").reflowWhenSupported(null).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").font(" ").build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").size(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").size(Double.NaN).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").characterSpacing(Double.POSITIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y").wordSpacing(Double.NEGATIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y")
                .stylePatch(new TextStylePatchRequest(null, null, null, null, null, null))
                .build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.builder()
                .coordinate(72, 144)
                .insert("x")
                .font("Helvetica")
                .size(12)
                .build()
                .validated());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(0, 72, 144, "x").font("Helvetica").size(12).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(1, Double.NaN, 144, "x").font("Helvetica").size(12).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(1, 72, Double.POSITIVE_INFINITY, "x").font("Helvetica").size(12).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(1, 72, 144, "x").rotationDegrees(Double.NaN).font("Helvetica").size(12).build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(1, 72, 144, "x").build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").build());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.at(1, 72, 144, "x").size(12).build());
        assertThrows(IllegalArgumentException.class, () ->
                new TextInsertRequest(
                        new TextInsertRequest.Target(new TextInsertRequest.AnchorTarget(
                                List.of(1),
                                new TextSelectorRequest("x", "y", null, null, null),
                                TextInsertRequest.Caret.after)),
                        "z",
                        TextInsertRequest.Style.anchor(),
                        null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextInsertRequest(null, "z", TextInsertRequest.Style.anchor(), null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextInsertRequest(
                        new TextInsertRequest.Target(new TextInsertRequest.AnchorTarget(
                                null,
                                new TextSelectorRequest("x", null, null, null, null),
                                TextInsertRequest.Caret.after)),
                        "z",
                        null,
                        null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextInsertRequest(
                        new TextInsertRequest.Target(new TextInsertRequest.AnchorTarget(
                                null,
                                new TextSelectorRequest("x", null, null, null, null),
                                TextInsertRequest.Caret.after)),
                        "z",
                        TextInsertRequest.Style.anchor(),
                        new TextLayoutRequest(TextLayoutRequest.Mode.sourceAnchored, TextLayoutRequest.Profile.DEFAULT)).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextInsertRequest(
                        new TextInsertRequest.Target(
                                new TextInsertRequest.AnchorTarget(null, new TextSelectorRequest("x", null, null, null, null), TextInsertRequest.Caret.after),
                                new TextInsertRequest.CoordinateTarget(1, 72.0, 144.0, null)),
                        "z",
                        TextInsertRequest.Style.anchor(),
                        null).validated());
    }
}
