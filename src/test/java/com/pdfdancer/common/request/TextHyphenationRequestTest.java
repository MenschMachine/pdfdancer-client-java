package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextHyphenationRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesEnabledDisabledAndOmittedOverrides() {
        TextReplaceRequest disabled = TextReplaceRequest.literal("x", "y")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .hyphenationEnabled(false)
                .build();
        TextDeleteRequest enabled = TextDeleteRequest.literal("x")
                .hyphenationEnabled(true)
                .reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                .build();
        TextStyleRequest inherited = TextStyleRequest.literal("x")
                .size(12)
                .reflowWhenSupported(TextLayoutRequest.Profile.DEFAULT)
                .build();

        JsonNode disabledJson = mapper.valueToTree(disabled);
        JsonNode enabledJson = mapper.valueToTree(enabled);
        JsonNode inheritedJson = mapper.valueToTree(inherited);

        assertFalse(disabledJson.at("/layout/hyphenationEnabled").asBoolean());
        assertTrue(enabledJson.at("/layout/hyphenationEnabled").asBoolean());
        assertTrue(inheritedJson.at("/layout/hyphenationEnabled").isMissingNode());
    }

    @Test
    void everyTextRequestBuilderExposesTheOverride() {
        TextLayoutRequest expected = TextLayoutRequest.requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .withHyphenationEnabled(false);

        assertEquals(expected, TextReplaceRequest.literal("x", "y")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .hyphenationEnabled(false)
                .build()
                .layout());
        assertEquals(expected, TextDeleteRequest.literal("x")
                .hyphenationEnabled(false)
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build()
                .layout());
        assertEquals(expected, TextInsertRequest.after("x", "y")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .hyphenationEnabled(false)
                .build()
                .layout());
        assertEquals(expected, TextStyleRequest.literal("x")
                .hyphenationEnabled(false)
                .size(12)
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build()
                .layout());
    }

    @Test
    void sourceAnchoredRejectsOverrideAndResetsPendingOverride() {
        assertThrows(IllegalArgumentException.class, () -> TextReplaceRequest.literal("x", "y")
                .sourceAnchored()
                .hyphenationEnabled(false)
                .build());
        // Hyphenation alone has no mode. The API treats that as reflowWhenSupported.
        JsonNode hyphenationOnly = mapper.valueToTree(TextDeleteRequest.literal("x")
                .hyphenationEnabled(true)
                .build());
        assertEquals("noReflow", hyphenationOnly.at("/layout/profile").asText());
        assertTrue(hyphenationOnly.at("/layout/hyphenationEnabled").asBoolean());
        assertTrue(hyphenationOnly.at("/layout/mode").isMissingNode());
        assertThrows(IllegalArgumentException.class, () -> TextInsertRequest.after("x", "y")
                .sourceAnchored()
                .hyphenationEnabled(false)
                .build());
        assertThrows(IllegalArgumentException.class, () -> TextStyleRequest.literal("x")
                .size(12)
                .sourceAnchored()
                .hyphenationEnabled(true)
                .build());

        TextLayoutRequest reset = TextReplaceRequest.literal("x", "y")
                .hyphenationEnabled(false)
                .sourceAnchored()
                .build()
                .layout();
        assertEquals(TextLayoutRequest.Mode.sourceAnchored, reset.mode());
        assertNull(reset.hyphenationEnabled());
    }

    @Test
    void explicitLayoutRetainsOverride() {
        TextLayoutRequest layout = new TextLayoutRequest(
                TextLayoutRequest.Mode.reflowWhenSupported,
                TextLayoutRequest.Profile.NO_REFLOW,
                true);

        TextLayoutRequest built = TextInsertRequest.after("x", "y")
                .layout(layout)
                .build()
                .layout();

        assertEquals(layout, built);
        assertTrue(built.hyphenationEnabled());
    }

    @Test
    void coordinateInsertRejectsHyphenationAndReflowLayouts() {
        // For a coordinate target, the API ignores a layout with no mode without an error.
        // It rejects an explicit reflow mode. The SDK rejects all of them.
        for (boolean enabled : new boolean[]{true, false}) {
            IllegalArgumentException noMode = assertThrows(IllegalArgumentException.class,
                    () -> TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12)
                            .hyphenationEnabled(enabled)
                            .build());
            assertTrue(noMode.getMessage().contains("target.coordinate"), noMode.getMessage());
            assertTrue(noMode.getMessage().contains("hyphenationEnabled"), noMode.getMessage());
        }
        IllegalArgumentException reflow = assertThrows(IllegalArgumentException.class,
                () -> TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12)
                        .reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                        .build());
        assertEquals("layout.mode reflowWhenSupported is not valid with target.coordinate", reflow.getMessage());
        IllegalArgumentException required = assertThrows(IllegalArgumentException.class,
                () -> TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12)
                        .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                        .build());
        assertEquals("layout.mode requireReflow is not valid with target.coordinate", required.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12)
                        .layout(new TextLayoutRequest(null, TextLayoutRequest.Profile.BODY_TEXT))
                        .build());
    }

    @Test
    void coordinateInsertAcceptsNoLayoutAndSourceAnchoredAndAnchorInsertAcceptsHyphenationAlone() {
        assertNull(TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12).build().layout());
        assertEquals(TextLayoutRequest.sourceAnchored(),
                TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12)
                        .sourceAnchored()
                        .build()
                        .layout());
        // A pending hyphenation value is cleared by sourceAnchored(), as for the other requests.
        assertEquals(TextLayoutRequest.sourceAnchored(),
                TextInsertRequest.at(1, 72, 144, "x").font("Helvetica").size(12)
                        .hyphenationEnabled(true)
                        .sourceAnchored()
                        .build()
                        .layout());
        // The API treats an omitted mode as reflowWhenSupported for an insert at an anchor.
        TextLayoutRequest anchor = TextInsertRequest.after("x", "y").hyphenationEnabled(true).build().layout();
        assertTrue(anchor.hyphenationEnabled());
        assertEquals(TextLayoutRequest.Profile.NO_REFLOW, anchor.profile());
    }
}
