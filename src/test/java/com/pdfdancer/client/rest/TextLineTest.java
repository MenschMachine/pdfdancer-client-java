package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.pdfdancer.client.rest.TestUtil.assertBetween;
import static org.junit.jupiter.api.Assertions.*;

public class TextLineTest extends BaseTest {

    @Test
    public void findLinesByPosition() {
        PDFDancer pdf = createClient();

        List<TextLineReference> lines = pdf.selectTextLines(); // across all pages
        assertEquals(340, lines.size());

        TextLineReference first = lines.get(0);
        assertEquals("TEXTLINE_000001", first.getInternalId());
        assertNotNull(first.getPosition());
        assertEquals(326, first.getPosition().getX().intValue());
        assertBetween(700, 720, first.getPosition().getY().intValue());

        TextLineReference last = lines.get(lines.size() - 1);
        assertNotNull(last.getPosition());
        assertEquals("TEXTLINE_000340", last.getInternalId());
        assertEquals(548, last.getPosition().getX().intValue());
        assertBetween(30, 40, last.getPosition().getY().intValue());
    }

    @Test
    public void findLinesByText() {

        PDFDancer pdf = createClient();

        List<TextLineReference> lines = pdf.page(1)
                .selectTextLinesStartingWith("the complete");
        assertEquals(1, lines.size());

        TextLineReference line = lines.get(0);
        assertEquals("TEXTLINE_000002", line.getInternalId());
        assertNotNull(line.getPosition());
        assertEquals(54, line.getPosition().getX().intValue());
        assertBetween(550, 620, line.getPosition().getY().intValue());
    }

    @Test
    public void findTextLinesByMatching() {
        PDFDancer pdf = createClient();

        // Test matching all text lines with wildcard pattern
        List<TextLineReference> lines = pdf.page(1).selectTextLinesMatching(".*");
        assertEquals(4, lines.size());

        // Test matching text lines with specific pattern
        lines = pdf.page(1).selectTextLinesMatching(".*Complete.*");
        assertEquals(1, lines.size());
        TextLineReference line = lines.get(0);
        assertEquals("TEXTLINE_000002", line.getInternalId());

        // Test matching with case-sensitive regex
        lines = pdf.page(1).selectTextLinesMatching("The Complete.*");
        assertEquals(1, lines.size());
    }

    @Test
    public void findSingularTextLineByMatching() {
        PDFDancer pdf = createClient();

        // Test finding a single text line by pattern
        Optional<TextLineReference> line = pdf.page(1).selectTextLineMatching(".*Complete.*");
        assertTrue(line.isPresent(), "Should find text line matching pattern");
        assertEquals("TEXTLINE_000002", line.get().getInternalId());

        // Test pattern with no matches
        Optional<TextLineReference> emptyResult = pdf.page(1).selectTextLineMatching(".*NonExistentText.*");
        assertFalse(emptyResult.isPresent(), "Should return empty Optional when no text line matches");
    }

    @Test
    public void findSingularTextLineByPosition() {
        PDFDancer pdf = createClient();

        // Test finding a single text line at a known position with sufficient epsilon
        Optional<TextLineReference> line = pdf.page(1).selectTextLineAt(54, 606, 1);
        assertTrue(line.isPresent(), "Should find text line at known position");
        assertEquals("TEXTLINE_000002", line.get().getInternalId());
        assertEquals(54, line.get().getPosition().getX().intValue());
        assertBetween(550, 620, line.get().getPosition().getY().intValue());

        // Test at position with no text line
        Optional<TextLineReference> emptyResult = pdf.page(1).selectTextLineAt(1000, 1000, 1);
        assertFalse(emptyResult.isPresent(), "Should return empty Optional when no text line found");
    }

    @Test
    public void deleteLine() {
        PDFDancer client = createClient();
        TextLineReference ref = client
                .page(1)
                .selectTextLinesStartingWith("the complete")
                .get(0);
        assertNotNull(ref);
        assertTrue(ref.delete());
        assertTrue(client.page(1)
                .selectTextLinesStartingWith("the complete").isEmpty());
        client.save("/tmp/deleteLine.pdf");
    }

    @Test
    public void moveLine() {
        PDFDancer client = createClient();
        TextLineReference ref = client
                .page(1)
                .selectTextLinesStartingWith("the complete")
                .get(0);

        Double originalX = ref.getPosition().getX();
        Double originalY = ref.getPosition().getY();
        assertTrue(ref.moveX(100));

        ref = client.page(1).selectTextLinesAt(originalX + 100, originalY).get(0);
        assertNotNull(ref);
        client.save("/tmp/moveLine.pdf");
    }

    @Test
    public void modifyLine() {
        PDFDancer client = createClient();
        TextLineReference ref = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(ref.edit().replace(" replaced ").apply());

        client.save("/tmp/modifyLine.pdf");

        assertTrue(client.page(1).selectTextLinesStartingWith("The Complete").isEmpty());

        assertFalse(client.page(1).selectTextLinesStartingWith(" replaced ").isEmpty());

        assertFalse(client.page(1).selectParagraphsStartingWith(" replaced ").isEmpty());

    }

    @Test
    public void modifyLineSimple() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(line.edit().replace("modified").apply());

        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertTextlineExists("modified", 1);
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
            TextLineReference line = client.page(1).selectTextLinesStartingWith(lineText).get(0);
            lineText = i + " The Complete C";
            assertTrue(line.edit().replace(lineText).apply());
        }

        new PDFAssertions(client).assertTextlineExists("9 The Complete C", 1);
    }

    @Test
    public void modifyLineWithFluentBuilder() {
        PDFDancer client = createClient();
        List<TextLineReference> matches = client.page(1).selectTextLinesMatching(".*Complete.*");
        assertEquals(1, matches.size());

        assertTrue(
                matches.get(0).edit()
                        .replace("This line was replaced!")
                        .font("Helvetica", 12.0)
                        .apply()
        );

        client.save("/tmp/modifyLineWithFluentBuilder.pdf");

        // Verify the text was changed
        assertTrue(client.page(1).selectTextLinesMatching(".*Complete.*").isEmpty());
        assertFalse(client.page(1).selectTextLinesStartingWith("This line was replaced!").isEmpty());
    }

    @Test
    public void modifyLineWithFont() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(
                line.edit()
                        .replace("Modified Line")
                        .font("Helvetica", 16.0)
                        .apply()
        );

        client.save("/tmp/modifyLineWithFont.pdf");

        new PDFAssertions(client)
                .assertTextlineExists("Modified Line", 1)
                .assertTextlineHasFont("Modified Line", "Helvetica", 16.0, 1);
    }

    @Test
    public void modifyLineWithColor() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(
                line.edit()
                        .color(new Color(255, 0, 0))
                        .apply()
        );

        client.save("/tmp/modifyLineWithColor.pdf");

        TextLineReference modifiedLine = client.page(1).selectTextLinesStartingWith("The Complete").get(0);
        Color color = modifiedLine.getColor();
        assertNotNull(color);
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void modifyLineWithPosition() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(
                line.edit()
                        .moveTo(100, 400)
                        .apply()
        );

        client.save("/tmp/modifyLineWithPosition.pdf");

        List<TextLineReference> movedLines = client.page(1).selectTextLinesAt(100, 400, 5);
        assertFalse(movedLines.isEmpty());
        assertEquals("The Complete", movedLines.get(0).getText().substring(0, 12));
    }

    @Test
    public void modifyLineWithAllProperties() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        assertTrue(
                line.edit()
                        .replace("Fully Modified Line")
                        .font("Courier", 14.0)
                        .color(new Color(0, 0, 255))
                        .moveTo(150, 450)
                        .apply()
        );

        client.save("/tmp/modifyLineWithAllProperties.pdf");

        // Verify all changes
        List<TextLineReference> modifiedLines = client.page(1).selectTextLinesAt(150, 450, 5);
        assertFalse(modifiedLines.isEmpty());
        TextLineReference modifiedLine = modifiedLines.get(0);

        assertEquals("Fully Modified Line", modifiedLine.getText());
        assertEquals("Courier", modifiedLine.getFontName());
        assertEquals(14.0, modifiedLine.getFontSize(), 0.1);

        Color color = modifiedLine.getColor();
        assertNotNull(color);
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    @Test
    public void modifyLineTextOnly() {
        PDFDancer client = createClient();
        TextLineReference line = client.page(1).selectTextLinesStartingWith("The Complete").get(0);

        String originalFont = line.getFontName();
        Double originalSize = line.getFontSize();

        assertTrue(
                line.edit()
                        .replace("Only Text Changed")
                        .apply()
        );

        TextLineReference modifiedLine = client.page(1).selectTextLinesStartingWith("Only Text Changed").get(0);
        assertEquals("Only Text Changed", modifiedLine.getText());
        // Font should remain unchanged when only text is modified
        assertEquals(originalFont, modifiedLine.getFontName());
        assertEquals(originalSize, modifiedLine.getFontSize(), 0.1);
    }

}
