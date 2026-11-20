package com.pdfdancer.common.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfdancer.common.request.MoveRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextObjectRefSerializationTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String TEXT_LINE_JSON = """
            {"internalId":"TEXT_LINE_1","objectRefType":"TEXT_LINE","type":"TEXT_LINE",
             "position":{"pageNumber":1,"boundingRect":{"x":72,"y":700,"width":120,"height":12}},
             "text":"Hello World","fontName":"Helvetica","fontSize":12}
            """;

    @Test
    void textLineDeserializesToTextObjectRefWithText() throws Exception {
        ObjectRef ref = mapper.readValue(TEXT_LINE_JSON, ObjectRef.class);

        TextObjectRef textRef = assertInstanceOf(TextObjectRef.class, ref);
        assertEquals("Hello World", textRef.getText());
        assertEquals("TEXT_LINE_1", textRef.getInternalId());
        assertEquals(ObjectType.TEXT_LINE, textRef.getType());
        assertEquals(72.0, textRef.getPosition().getX());
        assertEquals(700.0, textRef.getPosition().getY());
    }

    @Test
    void textIsOptional() throws Exception {
        ObjectRef ref = mapper.readValue("""
                {"internalId":"TEXT_LINE_2","objectRefType":"TEXT_LINE","type":"TEXT_LINE","position":{"pageNumber":1}}
                """, ObjectRef.class);

        assertNull(assertInstanceOf(TextObjectRef.class, ref).getText());
    }

    @Test
    void otherTypesStillDeserializeToPlainObjectRef() throws Exception {
        ObjectRef ref = mapper.readValue("""
                {"internalId":"IMAGE_1","objectRefType":"IMAGE","type":"IMAGE","position":{"pageNumber":1}}
                """, ObjectRef.class);

        assertEquals(ObjectRef.class, ref.getClass());
    }

    @Test
    void textIsNotSentBackToTheServer() throws Exception {
        ObjectRef ref = mapper.readValue(TEXT_LINE_JSON, ObjectRef.class);

        JsonNode move = mapper.valueToTree(new MoveRequest(ref, Position.atPageCoordinates(1, 10, 20)));

        assertEquals("TEXT_LINE_1", move.at("/objectRef/internalId").asText());
        assertFalse(move.at("/objectRef").has("text"), "text must be read-only metadata");
    }
}
