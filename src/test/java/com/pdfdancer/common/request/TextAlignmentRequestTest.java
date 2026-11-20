package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PdfAffineTransform;
import com.pdfdancer.common.response.TextEditResponse;
import com.pdfdancer.common.response.TextOperationDiagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextAlignmentRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode layoutOf(Object request) {
        return mapper.valueToTree(request).get("layout");
    }

    private static TextLayoutRequest noReflow() {
        return TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW);
    }

    @Test
    void alignmentValuesMatchTheApi() {
        assertEquals(
                List.of("left", "right", "centered", "justified"),
                java.util.Arrays.stream(TextLayoutRequest.Alignment.values())
                        .map(TextLayoutRequest.Alignment::value)
                        .toList());
    }

    @ParameterizedTest
    @EnumSource(TextLayoutRequest.Alignment.class)
    void alignmentAloneImpliesReflowWithNoReflowProfile(TextLayoutRequest.Alignment alignment) {
        JsonNode layout = layoutOf(TextReplaceRequest.literal("a", "b").alignment(alignment).build());

        assertEquals(3, layout.size());
        assertEquals("reflowWhenSupported", layout.get("mode").asText());
        assertEquals("noReflow", layout.get("profile").asText());
        assertEquals(alignment.value(), layout.get("alignment").asText());
    }

    @Test
    void alignmentCombinesWithExplicitLayoutInAnyOrder() {
        TextReplaceRequest alignmentFirst = TextReplaceRequest.literal("a", "b")
                .alignment(TextLayoutRequest.Alignment.RIGHT)
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build();
        TextReplaceRequest layoutFirst = TextReplaceRequest.literal("a", "b")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .alignment(TextLayoutRequest.Alignment.RIGHT)
                .build();

        for (TextReplaceRequest request : List.of(alignmentFirst, layoutFirst)) {
            JsonNode layout = layoutOf(request);
            assertEquals("requireReflow", layout.get("mode").asText());
            assertEquals("bodyText", layout.get("profile").asText());
            assertEquals("right", layout.get("alignment").asText());
        }
    }

    @Test
    void alignmentCombinesWithHyphenationAndExplicitLayout() {
        JsonNode layout = layoutOf(TextReplaceRequest.literal("a", "b")
                .reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                .hyphenationEnabled(false)
                .alignment(TextLayoutRequest.Alignment.CENTERED)
                .build());

        assertEquals("reflowWhenSupported", layout.get("mode").asText());
        assertEquals("noReflow", layout.get("profile").asText());
        assertEquals(false, layout.get("hyphenationEnabled").asBoolean());
        assertEquals("centered", layout.get("alignment").asText());
    }

    @Test
    void alignmentWithOnlyHyphenationSendsTheSameModeAsAlignmentAloneInAnyOrder() {
        TextReplaceRequest alignmentFirst = TextReplaceRequest.literal("a", "b")
                .alignment(TextLayoutRequest.Alignment.RIGHT)
                .hyphenationEnabled(false)
                .build();
        TextReplaceRequest hyphenationFirst = TextReplaceRequest.literal("a", "b")
                .hyphenationEnabled(false)
                .alignment(TextLayoutRequest.Alignment.RIGHT)
                .build();

        for (TextReplaceRequest request : List.of(alignmentFirst, hyphenationFirst)) {
            JsonNode layout = layoutOf(request);
            JsonNode alignmentAlone = layoutOf(TextReplaceRequest.literal("a", "b")
                    .alignment(TextLayoutRequest.Alignment.RIGHT)
                    .build());
            assertEquals("reflowWhenSupported", layout.get("mode").asText());
            assertEquals(alignmentAlone.get("mode"), layout.get("mode"));
            assertEquals(alignmentAlone.get("profile"), layout.get("profile"));
            assertEquals("noReflow", layout.get("profile").asText());
            assertEquals(false, layout.get("hyphenationEnabled").asBoolean());
            assertEquals("right", layout.get("alignment").asText());
        }
    }

    @Test
    void alignmentInsideALayoutObjectGoesWithLayoutAndSourceAnchoredButSurvivesReflowCalls() {
        TextLayoutRequest withRight = noReflow().withAlignment(TextLayoutRequest.Alignment.RIGHT);
        TextLayoutRequest plain = noReflow();

        // sourceAnchored() replaces the layout, and the alignment of that layout goes with it.
        TextLayoutRequest sourceAnchored = TextReplaceRequest.literal("a", "b")
                .layout(withRight)
                .sourceAnchored()
                .build()
                .layout();
        assertEquals(TextLayoutRequest.sourceAnchored(), sourceAnchored);
        // A later layout(...) without alignment replaces the earlier layout, including its alignment.
        assertEquals(plain, TextReplaceRequest.literal("a", "b")
                .layout(withRight).layout(plain).build().layout());
        // reflowWhenSupported(...) and requireReflow(...) change only the mode and the profile.
        // They keep the alignment of the layout object, as they keep its hyphenation value.
        assertEquals(TextLayoutRequest.requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .withAlignment(TextLayoutRequest.Alignment.RIGHT),
                TextReplaceRequest.literal("a", "b")
                        .layout(withRight)
                        .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .build().layout());
        assertEquals(TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                        .withAlignment(TextLayoutRequest.Alignment.RIGHT),
                TextReplaceRequest.literal("a", "b")
                        .layout(withRight)
                        .reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                        .build().layout());
        TextLayoutRequest withHyphenationAndRight = withRight.withHyphenationEnabled(false);
        assertEquals(TextLayoutRequest.requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .withHyphenationEnabled(false)
                        .withAlignment(TextLayoutRequest.Alignment.RIGHT),
                TextReplaceRequest.literal("a", "b")
                        .layout(withHyphenationAndRight)
                        .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .build().layout());
        // sourceAnchored() between two calls still drops the alignment of the earlier layout.
        assertEquals(TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT),
                TextReplaceRequest.literal("a", "b")
                        .layout(withRight)
                        .sourceAnchored()
                        .reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                        .build().layout());
        // alignment(...) after layout(...) replaces the alignment of that layout.
        assertEquals(TextLayoutRequest.Alignment.LEFT, TextReplaceRequest.literal("a", "b")
                .layout(withRight).alignment(TextLayoutRequest.Alignment.LEFT).build().layout().alignment());
    }

    @Test
    void alignmentSetWithAlignmentStillConflictsWithSourceAnchoredAfterALayoutCall() {
        TextLayoutRequest plain = noReflow();

        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("a", "b")
                .alignment(TextLayoutRequest.Alignment.RIGHT)
                .layout(plain)
                .sourceAnchored()
                .build());
    }

    @Test
    void replaceWithoutAlignmentHasNoAlignmentKey() {
        JsonNode layout = layoutOf(TextReplaceRequest.literal("a", "b")
                .reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                .build());

        assertTrue(layout.path("alignment").isMissingNode());
        assertTrue(layoutOf(TextReplaceRequest.literal("a", "b").build()) == null);
    }

    @Test
    void alignmentWithSourceAnchoredIsRejectedInAnyOrder() {
        IllegalArgumentException first = assertThrows(IllegalArgumentException.class,
                () -> TextReplaceRequest.literal("a", "b")
                        .alignment(TextLayoutRequest.Alignment.RIGHT)
                        .sourceAnchored()
                        .build());
        IllegalArgumentException second = assertThrows(IllegalArgumentException.class,
                () -> TextReplaceRequest.literal("a", "b")
                        .sourceAnchored()
                        .alignment(TextLayoutRequest.Alignment.RIGHT)
                        .build());

        assertTrue(first.getMessage().contains("layout.alignment is not valid"));
        assertTrue(second.getMessage().contains("layout.alignment is not valid"));
    }

    @Test
    void alignmentWithImageIsRejected() {
        Image image = new Image();
        image.setData(new byte[]{1});

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> TextReplaceRequest.builder()
                        .literal("{{logo}}")
                        .alignment(TextLayoutRequest.Alignment.RIGHT)
                        .replaceWithImage(image, PdfAffineTransform.builder().build())
                        .build());

        assertEquals("layout.alignment is not valid with replaceWithImage", error.getMessage());
    }

    @Test
    void layoutMethodCarriesAndKeepsAlignment() {
        TextLayoutRequest withAlignment = noReflow().withAlignment(TextLayoutRequest.Alignment.RIGHT);
        TextLayoutRequest plain = noReflow();

        assertEquals(withAlignment, TextReplaceRequest.literal("a", "b").layout(withAlignment).build().layout());
        // layout() keeps an alignment that alignment() set, in any order.
        assertEquals(withAlignment, TextReplaceRequest.literal("a", "b")
                .alignment(TextLayoutRequest.Alignment.RIGHT).layout(plain).build().layout());
        assertEquals(withAlignment, TextReplaceRequest.literal("a", "b")
                .layout(plain).alignment(TextLayoutRequest.Alignment.RIGHT).build().layout());
        // layout(null) keeps the alignment too. The alignment then uses the default layout.
        assertEquals(withAlignment, TextReplaceRequest.literal("a", "b")
                .alignment(TextLayoutRequest.Alignment.RIGHT).layout(null).build().layout());
        // A layout with its own alignment replaces the earlier alignment.
        assertEquals(TextLayoutRequest.Alignment.CENTERED, TextReplaceRequest.literal("a", "b")
                .alignment(TextLayoutRequest.Alignment.LEFT)
                .layout(plain.withAlignment(TextLayoutRequest.Alignment.CENTERED))
                .build().layout().alignment());
    }

    @Test
    void layoutRequestAlignmentSerializationAndValidation() throws Exception {
        TextLayoutRequest layout = noReflow().withAlignment(TextLayoutRequest.Alignment.RIGHT);

        assertEquals(TextLayoutRequest.Alignment.RIGHT, layout.alignment());
        assertEquals("right", mapper.valueToTree(layout).get("alignment").asText());
        assertEquals(layout, mapper.readValue(mapper.writeValueAsString(layout), TextLayoutRequest.class));
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.validateLayout(
                TextLayoutRequest.sourceAnchored().withAlignment(TextLayoutRequest.Alignment.LEFT)));
        assertThrows(IllegalArgumentException.class, () -> TextLayoutRequest.Alignment.fromValue("middle"));
    }

    @Test
    void layoutWithOmittedModeAndProfileEqualsItsOwnJsonRoundTrip() throws Exception {
        TextLayoutRequest layout = new TextLayoutRequest(null, null, true, null);

        // The default profile is part of the object, so object and JSON show the same value.
        assertEquals(TextLayoutRequest.Profile.NO_REFLOW, layout.profile());
        assertTrue(layout.toString().contains("profile=NO_REFLOW"), layout.toString());
        assertEquals("noReflow", mapper.valueToTree(layout).get("profile").asText());
        TextLayoutRequest roundTrip = mapper.readValue(mapper.writeValueAsString(layout), TextLayoutRequest.class);
        assertEquals(layout, roundTrip);
        assertEquals(layout.hashCode(), roundTrip.hashCode());
        assertEquals(new TextLayoutRequest(null, TextLayoutRequest.Profile.NO_REFLOW, true, null), layout);
        // An explicit mode without a profile keeps a null profile. validateLayout asks for it.
        assertEquals(null, new TextLayoutRequest(TextLayoutRequest.Mode.requireReflow, null).profile());
    }

    @Test
    void withHyphenationEnabledKeepsAlignment() {
        TextLayoutRequest layout = noReflow()
                .withAlignment(TextLayoutRequest.Alignment.RIGHT)
                .withHyphenationEnabled(true);

        assertEquals(TextLayoutRequest.Alignment.RIGHT, layout.alignment());
    }

    @Test
    void alignmentIsRejectedOnDeleteInsertAndStyle() {
        TextLayoutRequest layout = noReflow().withAlignment(TextLayoutRequest.Alignment.RIGHT);
        List<Supplier<Object>> builds = List.of(
                () -> TextDeleteRequest.literal("a").layout(layout).build(),
                () -> TextInsertRequest.after("a", "b").layout(layout).build(),
                () -> TextStyleRequest.literal("a").font("Helvetica").layout(layout).build());

        for (Supplier<Object> build : builds) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, build::get);
            assertTrue(error.getMessage().contains("valid only for text replace"), error.getMessage());
        }
    }

    @Test
    void omittedModeMeansReflowWhenSupportedAsInTheApi() {
        TextLayoutRequest layout = new TextLayoutRequest(
                null, TextLayoutRequest.Profile.NO_REFLOW, null, TextLayoutRequest.Alignment.RIGHT);

        TextReplaceRequest.validateLayout(layout);
        JsonNode json = mapper.valueToTree(layout);

        assertTrue(json.path("mode").isMissingNode());
        assertEquals("noReflow", json.get("profile").asText());
        assertEquals("right", json.get("alignment").asText());
    }

    @Test
    void omittedModeWithoutProfileUsesTheDefaultProfile() {
        TextLayoutRequest empty = new TextLayoutRequest(null, null, null, null);
        TextLayoutRequest hyphenation = new TextLayoutRequest(null, null, true, null);

        TextReplaceRequest.validateLayout(empty);
        TextReplaceRequest.validateLayout(hyphenation);

        assertEquals(1, mapper.valueToTree(empty).size());
        assertEquals("noReflow", mapper.valueToTree(empty).get("profile").asText());
        assertEquals("noReflow", mapper.valueToTree(hyphenation).get("profile").asText());
        assertTrue(mapper.valueToTree(hyphenation).get("hyphenationEnabled").asBoolean());
    }

    @Test
    void explicitReflowModeWithoutProfileAsksForAProfile() {
        IllegalArgumentException reflow = assertThrows(IllegalArgumentException.class,
                () -> TextReplaceRequest.validateLayout(
                        new TextLayoutRequest(TextLayoutRequest.Mode.reflowWhenSupported, null)));
        IllegalArgumentException require = assertThrows(IllegalArgumentException.class,
                () -> TextReplaceRequest.validateLayout(
                        new TextLayoutRequest(TextLayoutRequest.Mode.requireReflow, null)));

        assertTrue(reflow.getMessage().contains("reflowWhenSupported profile"));
        assertTrue(require.getMessage().contains("requireReflow profile"));
    }

    @Test
    void imageReplaceRejectsALayoutWithOmittedMode() {
        Image image = new Image();
        image.setData(new byte[]{1});

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> TextReplaceRequest.builder()
                        .literal("{{logo}}")
                        .layout(new TextLayoutRequest(null, TextLayoutRequest.Profile.NO_REFLOW))
                        .replaceWithImage(image, PdfAffineTransform.builder().build())
                        .build());

        assertTrue(error.getMessage().contains("only sourceAnchored"));
    }

    private static TextReplaceRequest imageRequest(TextLayoutRequest layout) {
        return new TextReplaceRequest(
                null,
                new TextSelectorRequest("{{logo}}", null, null, null, null),
                null,
                new TextReplacementImageRequest(new byte[]{1}, PdfAffineTransform.builder().build()),
                layout);
    }

    @Test
    void imageReplaceWithoutLayoutGetsASourceAnchoredLayout() {
        TextReplaceRequest request = imageRequest(null);

        // The layout is set when the request is created. A caller does not need validated().
        assertEquals(TextLayoutRequest.sourceAnchored(), request.layout());
        assertEquals("sourceAnchored", mapper.valueToTree(request).at("/layout/mode").asText());
        assertSame(request, request.validated());
        assertEquals(TextLayoutRequest.sourceAnchored(), request.validated().layout());
        assertEquals("sourceAnchored", mapper.valueToTree(request.validated()).at("/layout/mode").asText());
        assertEquals(TextLayoutRequest.sourceAnchored(), request.withPages(List.of(1)).validated().layout());
    }

    @Test
    void imageReplaceKeepsAnExplicitSourceAnchoredLayout() {
        TextReplaceRequest request = imageRequest(TextLayoutRequest.sourceAnchored());

        assertSame(request, request.validated());
    }

    @Test
    void alignmentNotHonoredCodeMatchesTheApi() throws Exception {
        assertEquals("TEXT_ALIGNMENT_NOT_HONORED", TextOperationDiagnostic.ALIGNMENT_NOT_HONORED);
        TextEditResponse response = mapper.readValue("""
                {"matched": 1, "changed": 0, "pagesChanged": [], "change": [], "warnings": [],
                 "errors": [{"page": 1, "code": "TEXT_ALIGNMENT_NOT_HONORED", "message": "m",
                             "elementIds": ["e"], "reflowUnitIds": ["r"]}]}
                """, TextEditResponse.class);

        assertEquals(TextOperationDiagnostic.ALIGNMENT_NOT_HONORED, response.errors().get(0).code());
    }
}
