package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Position;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PathTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "basic-paths.pdf";
    }


    @Test
    public void findPaths() {
        PDFDancer pdf = createClient();

        List<PathReference> paths = pdf.selectPaths();  // across entire document
        assertEquals(9, paths.size());

        PathReference path1 = paths.get(0);
        assertNotNull(path1);
        assertEquals("PATH_000001", path1.getInternalId());

        Position pos = path1.getPosition();
        assertEquals(80, pos.getX().intValue());
        assertEquals(720, pos.getY().intValue());
    }

    @Test
    public void findPathsByPosition() {
        PDFDancer pdf = createClient();

        // Page 0, hit-test at (80, 720)
        List<PathReference> paths = pdf.page(0).selectPathAt(80, 720);
        assertEquals(1, paths.size());
        assertEquals("PATH_000001", paths.get(0).getInternalId());
    }


    @Test
    public void deletePath() {
        PDFDancer client = createClient();
        List<PathReference> paths = client.page(0).selectPathAt(80, 720);
        assertEquals(1, paths.size());
        assertEquals("PATH_000001", paths.get(0).getInternalId());

        paths.get(0).delete();
        paths = client.page(0).selectPathAt(80, 720);
        assertEquals(0, paths.size());
        paths = client.selectPaths();
        assertEquals(8, paths.size());
    }

    @Test
    public void movePath() {

        PDFDancer client = createClient();
        List<PathReference> paths = client.page(0).selectPathAt(80, 720);
        PathReference objectRef = paths.get(0);
        Position position = objectRef.getPosition();

        assertEquals(80, position.getX().intValue());
        assertEquals(720, position.getY().intValue());

        assertTrue(objectRef.moveTo(50.1, 100));

        paths = client.page(0).selectPathAt(80, 720);
        assertTrue(paths.isEmpty());
        paths = client.page(0).selectPathAt(50.1, 100);

        objectRef = paths.get(0);
        position = objectRef.getPosition();

        assertEquals(50.1, position.getX(), 0.01);
        assertEquals(100, position.getY().intValue(), 0.01);
    }

    @Test
    public void addPath() throws IOException {
        PDFDancer pdf = TestPDFDancer.newPdf(getValidToken(), httpClient);
        pdf.page(0)
                .newLine()
                .from(100, 201.5)
                .to(50.5, 300)
                .color(Color.BLACK)
                .lineWidth(2.5d)
                .add();

        List<PathReference> paths = pdf.page(0).selectPathAt(100, 201.5);
        assertEquals(1, paths.size());
        assertEquals("PATH_000001", paths.get(0).getInternalId());
        pdf.save("/tmp/addPath.client");
    }

}
