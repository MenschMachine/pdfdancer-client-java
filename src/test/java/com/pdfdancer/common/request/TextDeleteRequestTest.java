package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextDeleteRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesLiteralSelector() {
        TextDeleteRequest request = TextDeleteRequest.literal("Confidential")
                .pages(1, 2)
                .caseSensitive(false)
                .wholeWords(true)
                .maxMatches(3)
                .sourceAnchored()
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("Confidential", json.at("/select/literal").asText());
        assertEquals(1, json.at("/pages/0").asInt());
        assertEquals(2, json.at("/pages/1").asInt());
        assertEquals(false, json.at("/select/caseSensitive").asBoolean());
        assertEquals(true, json.at("/select/wholeWords").asBoolean());
        assertEquals(3, json.at("/select/maxMatches").asInt());
        assertEquals("sourceAnchored", json.at("/layout/mode").asText());
    }

    @Test
    void serializesRegexSelectorWithReflowLayout() {
        TextDeleteRequest request = TextDeleteRequest.regex("\\bDRAFT\\b")
                .reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                .build();

        JsonNode json = mapper.valueToTree(request);

        assertEquals("\\bDRAFT\\b", json.at("/select/regex").asText());
        assertEquals("reflowWhenSupported", json.at("/layout/mode").asText());
        assertEquals("noReflow", json.at("/layout/profile").asText());
    }

    @Test
    void validatesSelectorAndOptions() {
        assertThrows(IllegalArgumentException.class, () -> TextDeleteRequest.literal("").build());
        assertThrows(IllegalArgumentException.class, () -> TextDeleteRequest.regex(" ").build());
        assertThrows(IllegalArgumentException.class, () -> TextDeleteRequest.literal("x").pages(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextDeleteRequest.literal("x").maxMatches(0).build());
        assertThrows(IllegalArgumentException.class, () -> TextDeleteRequest.literal("x").reflowWhenSupported(null).build());
        assertThrows(IllegalArgumentException.class, () ->
                new TextDeleteRequest(List.of(1),
                        new TextSelectorRequest("x", "y", null, null, null),
                        null).validated());
        assertThrows(IllegalArgumentException.class, () ->
                new TextDeleteRequest(null,
                        new TextSelectorRequest("x", null, null, null, null),
                        new TextLayoutRequest(TextLayoutRequest.Mode.sourceAnchored, TextLayoutRequest.Profile.DEFAULT)).validated());
    }
}
