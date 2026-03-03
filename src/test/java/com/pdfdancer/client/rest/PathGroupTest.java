package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

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
        PathGroupReference group = pdf.page(1).groupPaths("by-ids", pathIds);

        assertEquals("by-ids", group.getGroupId());
        assertEquals(2, group.getPathCount());
        assertNotNull(group.getBoundingBox());

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertPathIsAt("PATH_0_000001", 80, 720, 1);
    }

    @Test
    public void createGroupByRegion() {
        PDFDancer pdf = createClient();

        BoundingRect region = new BoundingRect(70, 710, 100, 100);
        PathGroupReference group = pdf.page(1).groupPathsInRegion("region-test", region);

        assertNotNull(group);
        assertEquals("region-test", group.getGroupId());
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

        pdf.page(1).groupPaths("move-test", pathIds);
        assertTrue(pdf.movePathGroup(0, "move-test", 200.0, 300.0));

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());
        assertEquals(200.0, groups.get(0).getX(), 0.01);
        assertEquals(300.0, groups.get(0).getY(), 0.01);

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertNoPathAt(80, 720, 1);
    }

    @Test
    public void groupAndRemove() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId());

        pdf.page(1).groupPaths("remove-test", pathIds);

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());

        assertTrue(pdf.removePathGroup(0, "remove-test"));

        groups = pdf.page(1).getPathGroups();
        assertTrue(groups.isEmpty());

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

        pdf.page(1).groupPaths("scale-test", pathIds);
        assertTrue(pdf.scalePathGroup(0, "scale-test", 2.0));

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1);
    }

    @Test
    public void rotatePathGroup() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());

        pdf.page(1).groupPaths("rotate-test", pathIds);
        assertTrue(pdf.rotatePathGroup(0, "rotate-test", 90.0));

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1)
                .assertNoPathAt(80, 720, 1);
    }

    @Test
    public void resizePathGroup() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        String pathId = paths.get(0).getInternalId();
        BoundingRect originalBounds = paths.get(0).getPosition().getBoundingRect();
        List<String> pathIds = List.of(pathId, paths.get(1).getInternalId());

        pdf.page(1).groupPaths("resize-test", pathIds);
        assertTrue(pdf.resizePathGroup(0, "resize-test", 50.0, 50.0));

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1);

        // Verify the path's bounding rect changed after resize
        List<PathReference> reloadedPaths = pdf.page(1).selectPaths();
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

        PathGroupReference group = pdf.page(1).groupPaths("scale-ref-test", pathIds);
        assertTrue(group.scale(0.5));

        new PDFAssertions(pdf)
                .assertNumberOfPaths(9, 1);
    }

    @Test
    public void moveAndRemoveViaReference() {
        PDFDancer pdf = createClient();
        List<PathReference> paths = pdf.page(1).selectPaths();
        List<String> pathIds = List.of(paths.get(0).getInternalId(), paths.get(1).getInternalId());

        PathGroupReference group = pdf.page(1).groupPaths("ref-test", pathIds);
        assertTrue(group.moveTo(150.0, 250.0));

        List<PathGroupReference> groups = pdf.page(1).getPathGroups();
        assertEquals(1, groups.size());
        assertEquals(150.0, groups.get(0).getX(), 0.01);
        assertEquals(250.0, groups.get(0).getY(), 0.01);

        assertTrue(group.remove());

        groups = pdf.page(1).getPathGroups();
        assertTrue(groups.isEmpty());

        new PDFAssertions(pdf)
                .assertNumberOfPaths(7, 1)
                .assertNoPathAt(80, 720, 1);
    }
}
