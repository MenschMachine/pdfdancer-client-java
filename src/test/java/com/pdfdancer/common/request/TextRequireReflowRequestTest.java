package com.pdfdancer.common.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextRequireReflowRequestTest {

    @Test
    void requireReflowIsAvailableForEveryTextRequestBuilder() {
        TextLayoutRequest expected = TextLayoutRequest.requireReflow(TextLayoutRequest.Profile.BODY_TEXT);

        assertEquals(expected, TextReplaceRequest.literal("x", "y")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build()
                .layout());
        assertEquals(expected, TextDeleteRequest.literal("x")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build()
                .layout());
        assertEquals(expected, TextInsertRequest.after("x", "y")
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build()
                .layout());
        assertEquals(expected, TextStyleRequest.literal("x")
                .size(12)
                .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
                .build()
                .layout());
    }
}
