package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests for moving text lines and for the unified moveTo/moveX/moveY helpers.
 */
public class TextLineMoveTest extends BaseTest {

    private static final double TOLERANCE = 0.05;

    /** A line with unique non-empty text well inside the page. */
    private static TextLineReference pickMovableLine(PDFDancer client) {
        List<TextLineReference> lines = client.page(1).selectTextLines().stream()
                .filter(line -> line.getText() != null && !line.getText().isBlank())
                .collect(Collectors.toList());
        Map<String, Long> counts = lines.stream()
                .collect(Collectors.groupingBy(TextLineReference::getText, Collectors.counting()));
        return lines.stream()
                .filter(line -> counts.get(line.getText()) == 1)
                .filter(line -> {
                    double x = line.getPosition().getX();
                    double y = line.getPosition().getY();
                    return y > 100 && y < 600 && x > 50 && x < 400;
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("No uniquely identifiable text line found on page 1"));
    }

    private static TextLineReference reread(PDFDancer client, String text) {
        List<TextLineReference> matches = client.page(1).selectTextLines().stream()
                .filter(line -> text.equals(line.getText()))
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "expected exactly one line with text '" + text + "'");
        return matches.get(0);
    }

    @Test
    public void selectTextLines() {
        PDFDancer client = createClient();

        List<TextLineReference> lines = client.selectTextLines();
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().allMatch(line -> line.type() == ObjectType.TEXT_LINE));
        assertTrue(lines.stream().anyMatch(line -> line.getText() != null));

        List<TextLineReference> pageLines = client.page(1).selectTextLines();
        assertFalse(pageLines.isEmpty());
        assertTrue(pageLines.stream().allMatch(line -> line.getPosition().getPageNumber() == 1));
    }

    @Test
    public void moveTextLineToAbsoluteCoordinates() {
        PDFDancer client = createClient();
        TextLineReference line = pickMovableLine(client);
        String text = line.getText();
        double newX = line.getPosition().getX() + 40;
        double newY = line.getPosition().getY() - 30;

        assertTrue(line.moveTo(newX, newY));

        TextLineReference moved = reread(client, text);
        assertEquals(newX, moved.getPosition().getX(), TOLERANCE);
        assertEquals(newY, moved.getPosition().getY(), TOLERANCE);
    }

    @Test
    public void relativeTextLineMovesComposeAndMatchRequestedOffsets() {
        PDFDancer client = createClient();
        TextLineReference line = pickMovableLine(client);
        String text = line.getText();
        double oldX = line.getPosition().getX();
        double oldY = line.getPosition().getY();

        assertTrue(line.moveX(20));
        assertTrue(line.moveY(-15));

        // Observed deltas must match the requested offsets (this catches any mismatch
        // between the reported bounding box and the line's text origin).
        TextLineReference moved = reread(client, text);
        assertEquals(oldX + 20, moved.getPosition().getX(), TOLERANCE);
        assertEquals(oldY - 15, moved.getPosition().getY(), TOLERANCE);
    }

    @Test
    public void deleteTextLine() {
        PDFDancer client = createClient();
        TextLineReference line = pickMovableLine(client);
        String text = line.getText();

        assertTrue(line.delete());

        assertTrue(client.page(1).selectTextLines().stream().noneMatch(l -> text.equals(l.getText())));
    }

    @Test
    public void relativeImageMovesMatchRequestedOffsets() {
        PDFDancer client = createClient("basic-image-test.pdf");
        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);
        double oldX = image.getPosition().getX();
        double oldY = image.getPosition().getY();

        assertTrue(image.moveX(20));
        assertTrue(image.moveY(-15));

        List<ImageReference> moved = client.page(1).selectImagesAt(oldX + 20, oldY - 15, 1);
        assertEquals(1, moved.size());
        assertEquals(oldX + 20, moved.get(0).getPosition().getX(), TOLERANCE);
        assertEquals(oldY - 15, moved.get(0).getPosition().getY(), TOLERANCE);
    }
}
