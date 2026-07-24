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
        assertThrows(IllegalArgumentException.class, () -> TextDeleteRequest.literal("x")
                .hyphenationEnabled(true)
                .build());
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
}
