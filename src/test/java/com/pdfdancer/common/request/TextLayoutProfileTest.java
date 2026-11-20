package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("deprecation")
class TextLayoutProfileTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void keepLineBreaksSendsTheNoReflowWireValue() {
        assertEquals("noReflow", TextLayoutRequest.Profile.KEEP_LINE_BREAKS.value());
    }

    @Test
    void deprecatedNoReflowSendsTheSameWireValue() {
        assertEquals("noReflow", TextLayoutRequest.Profile.NO_REFLOW.value());
    }

    @Test
    void lookupByWireValueReturnsTheNewConstant() {
        assertSame(TextLayoutRequest.Profile.KEEP_LINE_BREAKS, TextLayoutRequest.Profile.fromValue("noReflow"));
    }

    @Test
    void deprecatedAndNewConstantsProduceIdenticalLayoutJson() {
        JsonNode viaNew = mapper.valueToTree(
                TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.KEEP_LINE_BREAKS));
        JsonNode viaOld = mapper.valueToTree(
                TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW));

        assertEquals(viaNew, viaOld);
        assertEquals("noReflow", viaNew.get("profile").asText());
    }

    @Test
    void layoutWithoutModeOrProfileDefaultsToKeepLineBreaks() {
        assertEquals(TextLayoutRequest.Profile.KEEP_LINE_BREAKS,
                new TextLayoutRequest(null, null, null, null).profile());
    }
}
