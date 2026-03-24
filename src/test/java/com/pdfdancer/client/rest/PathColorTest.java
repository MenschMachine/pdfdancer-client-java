package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for path color modification functionality.
 * Verifies that stroke and fill colors can be modified via the PathReference.edit() builder.
 */
public class PathColorTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "basic-paths.pdf";
    }

    @Test
    public void modifyPathStrokeColor() {
        PDFDancer client = createClient();

        // Find a path at position (80, 720)
        List<PathReference> paths = client.page(1).selectPathsAt(80, 720);
        assertEquals(1, paths.size(), "Should find exactly one path at (80, 720)");

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();
        assertNotNull(internalId, "Path should have an internal ID");

        // Modify stroke color to red
        Color newStrokeColor = new Color(255, 0, 0);
        boolean result = path.edit()
                .strokeColor(newStrokeColor)
                .apply();

        assertTrue(result, "Path color modification should succeed");

        // Verify using PDFAssertions
        new PDFAssertions(client)
                .assertPathHasStrokeColor(internalId, newStrokeColor, 1);
    }

    @Test
    public void modifyPathFillColor() {
        PDFDancer client = createClient();

        // Find a path at position (80, 720)
        List<PathReference> paths = client.page(1).selectPathsAt(80, 720);
        assertEquals(1, paths.size());

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();

        // Modify fill color to blue
        Color newFillColor = new Color(0, 0, 255);
        boolean result = path.edit()
                .fillColor(newFillColor)
                .apply();

        assertTrue(result, "Path color modification should succeed");

        // Verify using PDFAssertions
        new PDFAssertions(client)
                .assertPathHasFillColor(internalId, newFillColor, 1);
    }

    @Test
    public void modifyPathPartialUpdatePreservesOtherColor() {
        PDFDancer client = createClient();

        // Find a path at position (80, 720)
        List<PathReference> paths = client.page(1).selectPathsAt(80, 720);
        assertEquals(1, paths.size(), "Should find exactly one path at (80, 720)");

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();
        assertNotNull(internalId, "Path should have an internal ID");

        // Capture original fill color before modification
        Color originalFillColor = path.getFillColor();

        // Modify stroke color only (fill color should be preserved)
        Color newStrokeColor = new Color(255, 0, 0);
        boolean result = path.edit()
                .strokeColor(newStrokeColor)
                .apply();

        assertTrue(result, "Path color modification should succeed");

        // Verify stroke color was changed
        new PDFAssertions(client)
                .assertPathHasStrokeColor(internalId, newStrokeColor, 1);

        // Verify fill color is unchanged (not cleared) - reload to get fresh state
        PDFDancer freshClient = createClient();
        List<PathReference> freshPaths = freshClient.page(1).selectPathsAt(80, 720);
        assertEquals(1, freshPaths.size());
        PathReference freshPath = freshPaths.get(0);

        // Fill color should be preserved (may be null if original was null, or same object if it existed)
        if (originalFillColor != null) {
            assertNotNull(freshPath.getFillColor(),
                    "Fill color should not be null after partial update");
            assertEquals(originalFillColor.getRed(), freshPath.getFillColor().getRed(), "Fill color red should be preserved");
            assertEquals(originalFillColor.getGreen(), freshPath.getFillColor().getGreen(), "Fill color green should be preserved");
            assertEquals(originalFillColor.getBlue(), freshPath.getFillColor().getBlue(), "Fill color blue should be preserved");
            assertEquals(originalFillColor.getAlpha(), freshPath.getFillColor().getAlpha(), "Fill color alpha should be preserved");
        } else {
            // If original was null, it should still be null (not cleared to a default)
            assertNull(freshPath.getFillColor(),
                    "Fill color should still be null after partial update when original was null");
        }
    }

    @Test
    public void modifyPathBothColors() {
        PDFDancer client = createClient();

        // Find a path at position (80, 720)
        List<PathReference> paths = client.page(1).selectPathsAt(80, 720);
        assertEquals(1, paths.size());

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();

        // Modify both stroke and fill colors
        Color newStrokeColor = new Color(255, 0, 0);
        Color newFillColor = new Color(0, 255, 0);
        boolean result = path.edit()
                .strokeColor(newStrokeColor)
                .fillColor(newFillColor)
                .apply();

        assertTrue(result, "Path color modification should succeed");

        // Verify using PDFAssertions
        new PDFAssertions(client)
                .assertPathHasStrokeColor(internalId, newStrokeColor, 1)
                .assertPathHasFillColor(internalId, newFillColor, 1);
    }

    @Test
    public void modifyPathWithAlpha() {
        PDFDancer client = createClient();

        // Find a path at position (80, 720)
        List<PathReference> paths = client.page(1).selectPathsAt(80, 720);
        assertEquals(1, paths.size());

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();

        // Modify stroke color with alpha
        Color newStrokeColor = new Color(255, 0, 0, 128);
        boolean result = path.edit()
                .strokeColor(newStrokeColor)
                .apply();

        assertTrue(result, "Path color modification with alpha should succeed");

        // Verify using PDFAssertions
        new PDFAssertions(client)
                .assertPathHasStrokeColor(internalId, newStrokeColor, 1);
    }

    @Test
    public void modifyMultiplePaths() {
        PDFDancer client = createClient();

        // Get all paths
        List<PathReference> allPaths = client.selectPaths();
        int originalCount = allPaths.size();
        assertTrue(originalCount > 0, "PDF should have at least one path");

        // Modify the first path's stroke color
        PathReference firstPath = allPaths.get(0);
        String firstPathId = firstPath.getInternalId();
        int firstPathPage = firstPath.getPosition().getPageNumber();
        Color newStrokeColor = new Color(128, 128, 128);
        boolean result = firstPath.edit()
                .strokeColor(newStrokeColor)
                .apply();

        assertTrue(result, "Modifying first path should succeed");

        // Verify path count remains the same
        List<PathReference> pathsAfter = client.selectPaths();
        assertEquals(originalCount, pathsAfter.size(), "Path count should remain the same after color modification");

        // Verify using PDFAssertions on the correct page
        new PDFAssertions(client)
                .assertPathHasStrokeColor(firstPathId, newStrokeColor, firstPathPage);
    }

    @Test
    public void modifyPathNoOpWithoutSettingColors() {
        PDFDancer client = createClient();

        // Find a path at position (80, 720)
        List<PathReference> paths = client.page(1).selectPathsAt(80, 720);
        assertEquals(1, paths.size(), "Should find exactly one path at (80, 720)");

        PathReference path = paths.get(0);
        String internalId = path.getInternalId();
        assertNotNull(internalId, "Path should have an internal ID");

        // Capture original colors
        Color originalStrokeColor = path.getStrokeColor();
        Color originalFillColor = path.getFillColor();

        // Call apply() without setting any colors - should be a no-op
        boolean result = path.edit().apply();

        assertTrue(result, "No-op path edit (no colors set) should succeed");

        // Verify colors are unchanged by re-fetching
        PDFDancer freshClient = createClient();
        List<PathReference> freshPaths = freshClient.page(1).selectPathsAt(80, 720);
        assertEquals(1, freshPaths.size());
        PathReference freshPath = freshPaths.get(0);

        // Stroke color should be unchanged
        if (originalStrokeColor != null) {
            assertNotNull(freshPath.getStrokeColor(), "Stroke color should not be null after no-op");
            assertEquals(originalStrokeColor.getRed(), freshPath.getStrokeColor().getRed(),
                    "Stroke color red should be unchanged after no-op");
            assertEquals(originalStrokeColor.getGreen(), freshPath.getStrokeColor().getGreen(),
                    "Stroke color green should be unchanged after no-op");
            assertEquals(originalStrokeColor.getBlue(), freshPath.getStrokeColor().getBlue(),
                    "Stroke color blue should be unchanged after no-op");
            assertEquals(originalStrokeColor.getAlpha(), freshPath.getStrokeColor().getAlpha(),
                    "Stroke color alpha should be unchanged after no-op");
        } else {
            assertNull(freshPath.getStrokeColor(),
                    "Stroke color should still be null after no-op");
        }

        // Fill color should be unchanged
        if (originalFillColor != null) {
            assertNotNull(freshPath.getFillColor(), "Fill color should not be null after no-op");
            assertEquals(originalFillColor.getRed(), freshPath.getFillColor().getRed(),
                    "Fill color red should be unchanged after no-op");
            assertEquals(originalFillColor.getGreen(), freshPath.getFillColor().getGreen(),
                    "Fill color green should be unchanged after no-op");
            assertEquals(originalFillColor.getBlue(), freshPath.getFillColor().getBlue(),
                    "Fill color blue should be unchanged after no-op");
            assertEquals(originalFillColor.getAlpha(), freshPath.getFillColor().getAlpha(),
                    "Fill color alpha should be unchanged after no-op");
        } else {
            assertNull(freshPath.getFillColor(),
                    "Fill color should still be null after no-op");
        }
    }
}