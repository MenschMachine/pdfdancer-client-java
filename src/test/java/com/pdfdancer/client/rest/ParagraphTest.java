package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.FontNotFoundException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.pdfdancer.client.rest.TestUtil.assertBetween;
import static org.junit.jupiter.api.Assertions.*;

public class ParagraphTest extends BaseTest {

    @Test
    public void findParagraphsByPosition() {
        PDFDancer client = createClient();
        List<TextParagraphReference> paragraphs = client.selectParagraphs();
        assertTrue(paragraphs.size() > 100 && paragraphs.size() < 150, "Should have more than 100 paragraphs");

        paragraphs = client.page(1).selectParagraphs();
        assertEquals(2, paragraphs.size());

        TextParagraphReference para1 = paragraphs.get(0);
        assertNotNull(para1.getPosition());
        assertEquals(326, para1.getPosition().getX().intValue());
        assertBetween(700, 720, para1.getPosition().getY().intValue());

        TextParagraphReference para2 = paragraphs.get(paragraphs.size() - 1);
        assertNotNull(para2.getPosition());
        assertEquals(54, para2.getPosition().getX().intValue());
        assertBetween(450, 500, para2.getPosition().getY().intValue());
    }

    @Test
    public void findParagraphsByText() {

        PDFDancer client = createClient();
        List<TextParagraphReference> paragraphs = client.page(1).selectParagraphsStartingWith("The Complete");
        assertEquals(1, paragraphs.size());

        TextParagraphReference para1 = paragraphs.get(0);
        assertNotNull(para1.getPosition());
        assertEquals(54, para1.getPosition().getX().intValue());
        assertBetween(450, 500, para1.getPosition().getY().intValue());

        paragraphs = client.page(1).selectParagraphsMatching(".*");
        assertEquals(2, paragraphs.size());

        paragraphs = client.page(1).selectParagraphsMatching(".*Complete.*");
        assertEquals(1, paragraphs.size());
    }

    @Test
    public void findSingularParagraphByPosition() {
        PDFDancer client = createClient();

        // Test finding a single paragraph at a known position with sufficient epsilon
        Optional<TextParagraphReference> paragraph = client.page(1).selectParagraphAt(54, 496, 1);
        assertTrue(paragraph.isPresent(), "Should find paragraph at known position");
        assertEquals(54, paragraph.get().getPosition().getX().intValue());
        assertBetween(450, 500, paragraph.get().getPosition().getY().intValue());

        // Test at position with no paragraph
        Optional<TextParagraphReference> emptyResult = client.page(1).selectParagraphAt(1000, 1000, 1);
        assertFalse(emptyResult.isPresent(), "Should return empty Optional when no paragraph found");
    }

    @Test
    public void deleteParagraph() {
        PDFDancer client = createClient();
        TextParagraphReference text = client.page(1).selectParagraphsStartingWith("The Complete").get(0);
        assertNotNull(text);
        assertTrue(text.delete());
        assertTrue(client.page(1).selectParagraphsStartingWith("The Complete").isEmpty());
    }

    @Test
    public void moveParagraph() {
        PDFDancer client = createClient();
        TextParagraphReference text = client.page(1).selectTextStartingWith("The Complete").get(0);

        text.moveTo(0.1, 300);

        // Optionally verify the moved paragraph now resides at that getPosition
        List<TextParagraphReference> relocated = client.page(1).selectParagraphsAt(0.1, 300);
        assertFalse(relocated.isEmpty());
    }

    @Test
    public void modifyParagraph() {
        PDFDancer client = createClient();
        TextParagraphReference ref = client.page(1).selectTextStartingWith("The Complete").get(0);

        // Apply complex modification (font, spacing, position, text)
        assertTrue(
                ref.edit().replace("Awesomely\nObvious!")
                        .font("Helvetica", 14)
                        .lineSpacing(0.7)
                        .moveTo(300.1, 500)
                        .apply()
        );

        // Verify modification succeeded by checking we can still find elements on the page
        assertFalse(client.page(1).selectParagraphs().isEmpty(), "Should still have paragraphs after modification");
    }

    @Test
    public void moveMultiFontTextLine() {
        PDFDancer client = createClient("Showcase.pdf");

        List<TextParagraphReference> relocated = client.page(1).selectParagraphsAt(0.1, 300);
        assertTrue(relocated.isEmpty());

        TextParagraphReference text = client.page(1).selectTextStartingWith("This is regular Sans text showing alignment and styles").get(0);
        // TODO this moves not only the line, but the whole paragraph
        // TODO write assertions to prove that
        // TODO also check color and stuff

        text.moveTo(0.1, 300);

        relocated = client.page(1).selectParagraphsAt(0.1, 300);
        assertFalse(relocated.isEmpty());

        new PDFAssertions(client)
                .assertParagraphExists("This is regular Sans text showing alignment and styles", 1)
                .assertParagraphIsAt("This is regular Sans text showing alignment and styles", 0.1, 300, 1, 3);

        saveTo(client, "moveMultiFontTextLine.pdf");
    }

    @Test
    public void moveMultiFontParagraph() {
        PDFDancer client = createClient("Showcase.pdf");

        List<TextParagraphReference> relocated = client.page(1).selectParagraphsAt(0.1, 300);
        assertTrue(relocated.isEmpty());

        TextParagraphReference text = client.page(1).selectParagraphsStartingWith("This is regular Sans text showing alignment and styles").get(0);
        text.moveTo(0.1, 300);

        relocated = client.page(1).selectParagraphsAt(0.1, 300);
        assertFalse(relocated.isEmpty());

        new PDFAssertions(client)
                .assertParagraphExists("This is regular Sans text showing alignment and styles", 1)
                .assertParagraphIsAt("This is regular Sans text showing alignment and styles", 0.1, 300, 1, 3);

        saveTo(client, "moveMultiFontParagraph.pdf");
    }


    @Test
    public void changeColorMultiFontParagraph() {
        PDFDancer client = createClient("Showcase.pdf");

        TextParagraphReference text = client.page(1).selectParagraphsStartingWith("This is regular Sans text showing alignment and styles").get(0);
        assertEquals(Color.BLACK, text.getColor());

        text.edit().color(Color.RED).apply();

        // TODO text rotation is lost
        saveTo(client, "changeColorMultiFontParagraph.pdf");

        new PDFAssertions(client)
                .assertParagraphHasColor("This is regular Sans text showing alignment and styles", Color.RED, 1);

    }

    @Test
    public void modifyParagraphSimple() {
        PDFDancer client = createClient();
        TextParagraphReference ref = client.page(1).selectTextStartingWith("The Complete").get(0);
        assertTrue(
                ref.edit().replace("Awesomely\nObvious!").apply()
        );
        assertNewParagraphExists(client);
    }

    private void assertNewParagraphExists(PDFDancer client) {
        new PDFAssertions(client).assertParagraphExists("Awesomely", 1);
    }

    @Test
    public void addParagraphWithCustomFont1() {
        PDFDancer client = createClient();

        try {
            assertTrue(client.newParagraph()
                    .text("Awesomely\nObvious!")
                    .font("Roboto", 14)
                    .lineSpacing(0.7)
                    .at(1, 300.1, 500)   // page 1, coordinates (x=300.1, y=500)
                    .add());
            fail("Should have thrown an FontNotFoundException");
        } catch (FontNotFoundException e) {
            assertEquals("Font not found: Roboto", e.getMessage());
        }
    }

    @Test
    public void addParagraphWithCustomFont1_1() {
        PDFDancer client = createClient();
        assertTrue(
                client.newParagraph()
                        .text("Awesomely\nObvious!")
                        .font(new Font("Roboto-Regular", 14))
                        .lineSpacing(0.7)
                        .at(1, 300.1, 500)
                        .add()
        );
        saveTo(client, "addParagraphWithCustomFont1_1.pdf");
        new PDFAssertions(client)
                .assertParagraphExists("Awesomely", 1);
    }

    @Test
    public void addParagraphWithCustomFont1_2() {
        PDFDancer client = createClient();
        List<Font> fonts = client.findFonts("Roboto", 14);
        assertFalse(fonts.isEmpty());
        Font roboto = fonts.get(0);
        assertTrue(roboto.getName().startsWith("Roboto"));

        assertTrue(
                client.newParagraph()
                        .text("Awesomely\nObvious!")
                        .font(roboto)
                        .lineSpacing(0.7)
                        .at(1, 300.1, 500)
                        .add()
        );
        assertNewParagraphExists(client);
    }

    @Test
    public void addParagraphWithCustomFont2() {
        // why is this failing but 1_2 is not?
        PDFDancer client = createClient();
        List<Font> fonts = client.findFonts("Asimovian", 14);
        assertFalse(fonts.isEmpty());
        assertEquals("Asimovian-Regular", fonts.get(0).getName());
        Font asimovian = fonts.get(0);

        assertTrue(
                client.newParagraph()
                        .text("Awesomely\nObvious!")
                        .font(asimovian)
                        .lineSpacing(0.7)
                        .at(1, 300.1, 500)
                        .add()
        );
        assertNewParagraphExists(client);
    }

    @Test
    public void addParagraphWithCustomFont3() {
        PDFDancer client = createClient();

        File ttfFile = new File("src/test/resources/fixtures/DancingScript-Regular.ttf");
        assertTrue(
                client.newParagraph()
                        .text("Awesomely\nObvious!")
                        .font(ttfFile, 24)
                        .lineSpacing(1.8)
                        .color(new Color(0, 0, 255))
                        .at(1, 300.1, 500)
                        .add()
        );
        assertNewParagraphExists(client);
    }

    @Test
    public void modifyParagraphWithoutPosition() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);
        double originalX = paragraph.getPosition().getX();
        double originalY = paragraph.getPosition().getY();

        assertTrue(
                paragraph.edit()
                        .replace("Awesomely\nObvious!")
                        .font("Helvetica", 12)
                        .lineSpacing(0.7)
                        .apply()
        );

        new PDFAssertions(client)
                .assertTextlineHasFont("Awesomely", "Helvetica", 12, 1)
                .assertTextlineHasFont("Obvious!", "Helvetica", 12, 1)
                .assertParagraphIsAt("Awesomely", originalX, originalY, 1, 3);
    }

    @Test
    public void modifyParagraphWithoutPositionAndSpacing() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);
        double originalX = paragraph.getPosition().getX();
        double originalY = paragraph.getPosition().getY();

        assertTrue(
                paragraph.edit()
                        .replace("Awesomely\nObvious!")
                        .font("Helvetica", 12)
                        .apply()
        );

        saveTo(client, "modifyParagraphWithoutPositionAndSpacing.pdf");
        new PDFAssertions(client)
                .assertTextlineHasFont("Awesomely", "Helvetica", 12, 1)
                .assertTextlineHasFont("Obvious!", "Helvetica", 12, 1)
                .assertParagraphIsAt("Awesomely", originalX, originalY, 1, 3);
    }

    @Test
    public void modifyParagraphNoop() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);

        assertTrue(paragraph.edit().apply());

        new PDFAssertions(client)
                .assertTextlineExists("The Complete", 1);
    }

    @Test
    public void modifyParagraphOnlyText() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);

        assertTrue(
                paragraph.edit()
                        .replace("lorem\nipsum\nCaesar")
                        .apply()
        );

        new PDFAssertions(client)
                .assertTextlineDoesNotExist("The Complete", 1)
                .assertTextlineExists("lorem", 1)
                .assertTextlineExists("ipsum", 1)
                .assertTextlineExists("Caesar", 1);
    }

    @Test
    public void modifyParagraphOnlyFont() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);

        assertTrue(
                paragraph.edit()
                        .font("Helvetica", 28)
                        .apply()
        );

        saveTo(client, "modifyParagraphOnlyFont.pdf");
        new PDFAssertions(client)
                .assertTextlineHasFont("The Complete", "Helvetica", 28, 1);
    }

    @Test
    public void modifyParagraphOnlyMove() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);

        assertTrue(
                paragraph.edit()
                        .moveTo(40, 40)
                        .apply()
        );

        saveTo(client, "modifyParagraphOnlyMove.pdf");
        new PDFAssertions(client)
                .assertParagraphIsAt("The Complete", 40, 12 // adjust for baseline vs bounding box difference
                        , 1, 3);
    }

    @Test
    public void paragraphColorReading() {
        PDFDancer client = createClient();
        TextParagraphReference paragraph = client.page(1).selectParagraphsStartingWith("The Complete").get(0);

        Color color = paragraph.getColor();
        assertNotNull(color);
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }
}
