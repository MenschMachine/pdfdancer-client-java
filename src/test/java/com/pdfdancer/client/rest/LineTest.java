package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LineTest extends BaseTest {

    @Test
    public void findLinesByPosition() {
        PDFDancer pdf = createClient();

        List<TextLineReference> lines = pdf.selectTextLines(); // across all pages
        assertEquals(340, lines.size());

        TextLineReference first = lines.get(0);
        assertEquals("TEXTLINE_000001", first.getInternalId());
        assertNotNull(first.getPosition());
        assertEquals(326, first.getPosition().getX().intValue());
        assertEquals(706, first.getPosition().getY().intValue());

        TextLineReference last = lines.get(lines.size() - 1);
        assertNotNull(last.getPosition());
        assertEquals("TEXTLINE_000340", last.getInternalId());
        assertEquals(548, last.getPosition().getX().intValue());
        assertEquals(35, last.getPosition().getY().intValue());
    }

    @Test
    public void findLinesByText() {

        PDFDancer pdf = createClient();

        List<TextLineReference> lines = pdf.page(0)
                .selectTextLinesStartingWith("the complete");
        assertEquals(1, lines.size());

        TextLineReference line = lines.get(0);
        assertEquals("TEXTLINE_000002", line.getInternalId());
        assertNotNull(line.getPosition());
        assertEquals(54, line.getPosition().getX().intValue());
        assertEquals(606, line.getPosition().getY().intValue());
    }

    @Test
    public void findSingularTextLineByPosition() {
        PDFDancer pdf = createClient();

        // Test finding a single text line at a known position with sufficient epsilon
        Optional<TextLineReference> line = pdf.page(0).selectSingleTextLineAt(54, 606, 1);
        assertTrue(line.isPresent(), "Should find text line at known position");
        assertEquals("TEXTLINE_000002", line.get().getInternalId());
        assertEquals(54, line.get().getPosition().getX().intValue());
        assertEquals(606, line.get().getPosition().getY().intValue());

        // Test at position with no text line
        Optional<TextLineReference> emptyResult = pdf.page(0).selectSingleTextLineAt(1000, 1000, 1);
        assertFalse(emptyResult.isPresent(), "Should return empty Optional when no text line found");
    }

    @Test
    public void deleteLine() {
        PDFDancer client = createClient();
        TextLineReference ref = client
                .page(0)
                .selectTextLinesStartingWith("the complete")
                .get(0);
        assertNotNull(ref);
        assertTrue(ref.delete());
        assertTrue(client.page(0)
                .selectTextLinesStartingWith("the complete").isEmpty());
        client.save("/tmp/deleteLine.client");
    }

    @Test
    public void moveLine() {
        PDFDancer client = createClient();
        TextLineReference ref = client
                .page(0)
                .selectTextLinesStartingWith("the complete")
                .get(0);

        Double originalX = ref.getPosition().getX();
        Double originalY = ref.getPosition().getY();
        assertTrue(ref.moveX(100));

        ref = client.page(0).selectTextLineAt(originalX + 100, originalY).get(0);
        assertNotNull(ref);
        client.save("/tmp/moveLine.client");
    }

    @Test
    public void modifyLine() {
        PDFDancer client = createClient();
        TextLineReference ref = client.page(0).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(ref.edit().replace(" replaced "));

        client.save("/tmp/modifyLine.client");

        assertTrue(client.page(0).selectTextLinesStartingWith("The Complete").isEmpty());

        assertFalse(client.page(0).selectTextLinesStartingWith(" replaced ").isEmpty());

        assertFalse(client.page(0).selectParagraphsStartingWith(" replaced ").isEmpty());

    }

    @Test
    public void modifyLineWithFont() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(0).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(line.edit().replace("modified"));

        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 0)
                .assertTextlineExists("modified", 0);
    }

    @Test
    public void findLinesByPositionMulti() {
        PDFDancer client = createClient();

        for (int i = 0; i < 10; i++) {
            List<TextLineReference> lines = client.selectTextLines();
            assertFalse(lines.isEmpty());
        }
    }

    @Test
    public void modifyLineMulti() {
        PDFDancer client = createClient();
        String lineText = "The Complete";

        for (int i = 0; i < 10; i++) {
            TextLineReference line = client.page(0).selectTextLinesStartingWith(lineText).get(0);
            lineText = i + " The Complete C";
            assertTrue(line.edit().replace(lineText));
        }

        new PDFAssertions(client).assertTextlineExists("9 The Complete C", 0);
    }

}
