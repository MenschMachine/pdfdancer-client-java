package com.pdfdancer.common.response;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadingUnitModelTest {
    @Test
    void unknownEnumValuesPreserveRawValues() {
        ReadingUnit unit = new ReadingUnit("u1", "SIDEBAR", "Body", null, null,
                java.util.List.of(new ReadingUnitRelationship("SIDEBAR_FOR", "u2")));

        assertEquals(ReadingUnitRole.UNKNOWN, unit.role());
        assertEquals("SIDEBAR", unit.rawRole());
        assertEquals(ReadingUnitRelationshipType.UNKNOWN, unit.relationships().get(0).type());
        assertEquals("SIDEBAR_FOR", unit.relationships().get(0).rawType());
    }

    @Test
    void deserializesCompleteDocumentResponse() throws Exception {
        String json = """
                {"pageCount":1,"mode":"PRIMARY","pages":[{"pageNumber":1,"mode":"PRIMARY","units":[
                {"id":"u1","role":"PARAGRAPH","text":"Body","stream":{"PRIMARY":{"included":true,"order":1}},
                "provenance":{"pageNumber":1,"sourceElementIds":["text-1"],"bounds":{"x":10,"y":20,"width":30,"height":40}},
                "relationships":[{"type":"CAPTION_FOR","targetUnitId":"u2"}]}]}]}
                """;
        ReadingUnitDocumentAnalysis result = new ObjectMapper().findAndRegisterModules()
                .readValue(json, ReadingUnitDocumentAnalysis.class);
        ReadingUnit unit = result.pages().get(0).units().get(0);

        assertEquals(1, result.pageCount());
        assertEquals("Body", unit.text());
        assertEquals(1, unit.stream().get(ReadingUnitMode.PRIMARY).order());
        assertEquals(30, unit.provenance().bounds().width());
        assertEquals(ReadingUnitRelationshipType.CAPTION_FOR, unit.relationships().get(0).type());
    }
}
