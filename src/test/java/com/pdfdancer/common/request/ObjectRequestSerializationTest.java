package com.pdfdancer.common.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.Size;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectRequestSerializationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void findRequestUsesV2FieldNames() {
        JsonNode json = mapper.valueToTree(new FindRequest(
                ObjectType.IMAGE, Position.atPageCoordinates(1, 10, 20), "hint"));

        assertEquals("IMAGE", json.at("/objectType").asText());
        assertEquals(1, json.at("/position/pageNumber").asInt());
        assertEquals("hint", json.at("/hint").asText());
        assertEquals(3, json.size());
    }

    @Test
    void deleteAndMoveRequestsWrapObjectReferencesUnderCanonicalKeys() {
        ObjectRef ref = ObjectRef.create(
                "test-id", Position.atPage(1), ObjectType.IMAGE, ObjectType.IMAGE);

        JsonNode delete = mapper.valueToTree(new DeleteRequest(ref));
        assertTrue(delete.has("objectRef"));
        assertEquals("test-id", delete.at("/objectRef/internalId").asText());

        JsonNode move = mapper.valueToTree(
                new MoveRequest(ref, Position.atPageCoordinates(2, 50, 60)));
        assertTrue(move.has("objectRef"));
        assertTrue(move.has("newPosition"));
        assertFalse(move.has("position"));
        assertEquals(2, move.at("/newPosition/pageNumber").asInt());
    }

    @Test
    void addAndModifyRequestsUseCanonicalObjectKeys() {
        Position position = Position.atPageCoordinates(1, 10, 20);
        Image image = new Image(null, "PNG", new Size(1, 1), position, new byte[]{1});
        ObjectRef ref = ObjectRef.create("test-id", position, ObjectType.IMAGE, ObjectType.IMAGE);

        JsonNode add = mapper.valueToTree(new AddRequest(image));
        assertTrue(add.has("object"));
        assertFalse(add.has("pdfObject"));

        JsonNode modify = mapper.valueToTree(new ModifyRequest(ref, image));
        assertTrue(modify.has("ref"));
        assertTrue(modify.has("newObject"));
        assertFalse(modify.has("objectRef"));
    }
}
