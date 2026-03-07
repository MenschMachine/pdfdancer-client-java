package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ClippingTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "Showcase.pdf";
    }

    @Test
    public void clearClippingOnPathThenMove() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        assertFalse(paths.isEmpty(), "Expected at least one path on page 1");

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();
        double originalX = path.getPosition().getX();
        double originalY = path.getPosition().getY();

        assertTrue(path.clearClipping());
        assertTrue(path.moveTo(410.25, 123.75));

        PathReference movedPath = pdf.page(1).selectPaths().stream()
                .filter(p -> internalId.equals(p.getInternalId()))
                .findFirst()
                .orElseThrow();
        double movedX = movedPath.getPosition().getX();
        double movedY = movedPath.getPosition().getY();
        assertNotEquals(originalX, movedX, 0.01);
        assertNotEquals(originalY, movedY, 0.01);

        new PDFAssertions(pdf)
                .assertNumberOfPaths(paths.size(), 1)
                .assertPathWithIdIsAt(internalId, movedX, movedY, 1, 0.5);
    }

    @Test
    public void clearClippingOnTextImageAndParagraphReferences() {
        PDFDancer pdf = createClient();

        TextLineReference textLine = pdf.page(1).selectTextLines().get(0);
        ImageReference image = pdf.page(1).selectImages().get(0);
        TextParagraphReference paragraph = pdf.page(1).selectParagraphs().get(0);

        String textPrefix = textLine.getText().substring(0, Math.min(12, textLine.getText().length()));
        String paragraphPrefix = paragraph.getText().substring(0, Math.min(12, paragraph.getText().length()));
        String imageId = image.getInternalId();
        double imageX = image.getPosition().getX();
        double imageY = image.getPosition().getY();

        assertTrue(textLine.clearClipping());
        assertTrue(image.clearClipping());
        assertTrue(paragraph.clearClipping());

        new PDFAssertions(pdf)
                .assertTextlineExists(Pattern.quote(textPrefix), 1)
                .assertParagraphExists(Pattern.quote(paragraphPrefix), 1)
                .assertImageWithIdAt(imageId, imageX, imageY, 1);
    }

    @Test
    public void clearClippingOnPathGroupReference() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        assertTrue(paths.size() >= 3, "Expected at least three paths on page 1");

        PathReference firstPath = paths.get(0);
        double originalX = firstPath.getPosition().getX();
        double originalY = firstPath.getPosition().getY();

        List<String> pathIds = List.of(
                paths.get(0).getInternalId(),
                paths.get(1).getInternalId(),
                paths.get(2).getInternalId()
        );

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertNotNull(group.getGroupId());
        assertTrue(group.clearClipping());
        assertTrue(group.moveTo(240.0, 360.0));

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());
        assertEquals(240.0, groups.get(0).getX(), 0.5);
        assertEquals(360.0, groups.get(0).getY(), 0.5);

        new PDFAssertions(pdf)
                .assertNumberOfPaths(paths.size(), 1)
                .assertNoPathAt(originalX, originalY, 1);
    }
}
