package com.pdfdancer.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextResponseToStringTest {
    private static final String JSON = """
            {"matched": 1, "changed": 0, "pagesChanged": [],
             "change": [{"page": 1, "operation": "replace", "sourceText": "source line",
                         "resultText": "result line", "requestedLayoutMode": "reflowWhenSupported",
                         "requestedLayoutProfile": "noReflow", "effectiveHyphenationEnabled": true,
                         "appliedLayoutMode": "REFLOWED", "elementIds": ["e1"],
                         "generatedElementIds": [], "reflowUnitIds": ["r1"]}],
             "warnings": [{"page": 1, "code": "A_WARNING", "message": "a warning message"}],
             "errors": [{"page": 2, "code": "TEXT_ALIGNMENT_NOT_HONORED", "message": "cannot align",
                         "elementIds": ["e2"], "reflowUnitIds": ["r2"]}]}
            """;

    @Test
    void responseToStringShowsCountsChangesWarningsAndErrors() throws Exception {
        TextEditResponse response = new ObjectMapper().readValue(JSON, TextEditResponse.class);

        String text = response.toString();

        assertTrue(text.contains("matched=1"), text);
        assertTrue(text.contains("changed=0"), text);
        assertTrue(text.contains("code=TEXT_ALIGNMENT_NOT_HONORED"), text);
        assertTrue(text.contains("message=cannot align"), text);
        assertTrue(text.contains("code=A_WARNING"), text);
        assertTrue(text.contains("sourceText=source line"), text);
        assertTrue(text.contains("appliedLayoutMode=REFLOWED"), text);
        assertTrue(text.contains("effectiveHyphenationEnabled=true"), text);
        assertFalse(text.contains("@"), text);
    }

    @Test
    void diagnosticToStringShowsEveryField() throws Exception {
        TextEditResponse response = new ObjectMapper().readValue(JSON, TextEditResponse.class);

        String error = response.errors().get(0).toString();
        String change = response.change().get(0).toString();

        assertTrue(error.startsWith("TextOperationDiagnostic["), error);
        assertTrue(error.contains("page=2"), error);
        assertTrue(error.contains("elementIds=[e2]"), error);
        assertTrue(error.contains("reflowUnitIds=[r2]"), error);
        assertTrue(change.startsWith("TextEditChangeDiagnostic["), change);
        assertTrue(change.contains("requestedLayoutProfile=noReflow"), change);
        assertTrue(change.contains("reflowUnitIds=[r1]"), change);
    }
}
