package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestUtilTest extends BaseTest {

    @Test
    public void testDrawCoordinateGrid() throws IOException {
        PDFDancer pdf = newPdf();

        TestUtil.drawCoordinateGrid(pdf);

        assertFalse(pdf.selectPaths().isEmpty(), "Should have drawn coordinate grid paths");
        assertFalse(pdf.page(1).selectPathsAt(50, 0).isEmpty(), "Missing X tick at x=50");
        assertFalse(pdf.page(1).selectPathsAt(0, 50).isEmpty(), "Missing Y tick at y=50");

        pdf.save("/tmp/test-util-coordinate-grid.pdf");
    }

    @Test
    public void testDrawCoordinateGridOnSpecificPage() throws IOException {
        PDFDancer pdf = newPdf();

        TestUtil.drawCoordinateGrid(pdf, 1);

        assertFalse(pdf.selectPaths().isEmpty(), "Should have drawn coordinate grid paths");
        assertFalse(pdf.page(1).selectPathsAt(10, 10).isEmpty(), "Missing grid intersection at (10,10)");

        pdf.save("/tmp/test-util-coordinate-grid-page0.pdf");
    }
}
