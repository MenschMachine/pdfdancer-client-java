package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PathGroupTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "basic-paths.pdf";
    }

    @Test
    public void createGroupByPathIds() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        assertTrue(paths.size() >= 2);

        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());
        PathGroupReference group = pdf.page(1).groupPaths(pathIds);

        assertNotNull(group.getGroupId());
        assertEquals(2, group.getPathCount());
        assertNotNull(group.getBoundingBox());

        // Grouping without move should not change the PDF
        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathIsAt("PATH_0_000001", 80, 720, 1);
    }

    @Test
    public void groupPathsAutoId() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();

        List<String> pathIds = List.of(paths.get(0).getInternalId());
        PathGroupReference group = pdf.page(1).groupPaths(pathIds);

        assertNotNull(group.getGroupId());
        assertEquals(1, group.getPathCount());

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathIsAt("PATH_0_000001", 80, 720, 1);
    }

    @Test
    public void groupPathsInRegion() {
        PDFDancer pdf = createClient();

        BoundingRect region = new BoundingRect(70, 710, 100, 100);
        PathGroupReference group = pdf.page(1).groupPathsInRegion(region);

        assertNotNull(group);
        assertNotNull(group.getGroupId());
        assertTrue(group.getPathCount() > 0);

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathIsAt("PATH_0_000001", 80, 720, 1);
    }

    @Test
    public void listEmptyGroups() {
        PDFDancer pdf = createClient();
        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertNotNull(groups);
        assertTrue(groups.isEmpty());

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathIsAt("PATH_0_000001", 80, 720, 1);
    }

    @Test
    public void groupAndMove() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.moveTo(200.0, 300.0));

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());
        assertEquals(200.0, groups.get(0).getX(), 0.01);
        assertEquals(300.0, groups.get(0).getY(), 0.01);

        // Paths should have moved away from original positions
        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertNoPathAt(80, 720, 1);
    }

    @Test
    public void groupAndRemove() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId());

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());

        assertTrue(group.remove());

        groups = pdf.page(1).getPathGroups();
        assertTrue(groups.isEmpty());

        // Removing a group deletes its paths from the PDF
        new PDFAssertions(pdf)
                .assertNumberOfPaths(8, 1)
                .assertNoPathAt(80, 720, 1);
    }

    @Test
    public void scalePathGroup() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        String pathId = paths.get(0).getInternalId();
        List<String> pathIds = List.of(pathId, paths.get(1).getInternalId());

        // Record original bounds of first path
        BoundingRect originalBounds = paths.get(0).getPosition().getBoundingRect();
        double origW = originalBounds.getWidth();
        double origH = originalBounds.getHeight();

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.scale(2.0));

        // After scaling 2x, path bounds should roughly double
        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathHasBounds(pathId, origW * 2, origH * 2, 1, 2.0);
    }

    @Test
    public void rotatePathGroup() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.rotate(90.0));

        // Paths should have moved from original positions after 90° rotation
        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertNoPathAt(80, 720, 1);
    }

    @Test
    public void resizePathGroup() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        String pathId = paths.get(0).getInternalId();
        List<String> pathIds = List.of(pathId, paths.get(1).getInternalId());

        // Record original bounds
        BoundingRect originalBounds = paths.get(0).getPosition().getBoundingRect();

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.resize(50.0, 50.0));

        // After resize, path bounds should have changed from original
        PDFAssertions assertions = new PDFAssertions(pdf);
        assertions.assertNumberOfPaths(9, 1);

        // Verify the path's bounding rect actually changed
        List<PathReference> reloadedPaths = assertions.getPdf().page(1).selectPaths();
        PathReference reloaded = reloadedPaths.stream()
                .filter(p -> pathId.equals(p.getInternalId()))
                .findFirst().orElseThrow();
        BoundingRect newBounds = reloaded.getPosition().getBoundingRect();
        assertNotEquals(originalBounds, newBounds, "Path bounds should change after resize");
    }

    @Test
    public void scaleViaReference() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        String pathId = paths.get(0).getInternalId();
        List<String> pathIds = List.of(pathId, paths.get(1).getInternalId());

        // Record original bounds
        BoundingRect originalBounds = paths.get(0).getPosition().getBoundingRect();
        double origW = originalBounds.getWidth();
        double origH = originalBounds.getHeight();

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.scale(0.5));

        // After scaling 0.5x, path bounds should roughly halve
        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathHasBounds(pathId, origW * 0.5, origH * 0.5, 1, 2.0);
    }

    @Test
    public void rotateViaReference() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.rotate(45));

        // 45° rotation should move paths from original position
        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertNoPathAt(80, 720, 1);
    }

    @Test
    public void moveAndRemoveViaReference() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());

        PathGroupReference group = pdf.page(1).groupPaths(pathIds);
        assertTrue(group.moveTo(150.0, 250.0));

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());
        assertEquals(150.0, groups.get(0).getX(), 0.01);
        assertEquals(250.0, groups.get(0).getY(), 0.01);

        assertTrue(group.remove());

        groups = pdf.page(1).getPathGroups();
        assertTrue(groups.isEmpty());

        // Move then remove: paths are deleted from the PDF
        new PDFAssertions(pdf)
                .assertNumberOfPaths(7, 1)
                .assertNoPathAt(80, 720, 1);
    }
}
