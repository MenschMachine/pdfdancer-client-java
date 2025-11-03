package com.tfc.pdf.pdfdancer.api.client.rest;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestUtilTest extends BaseTest {

    @Test
    public void testDrawCoordinateGrid() throws IOException {
        PDFDancer pdf = newPdf();

        TestUtil.drawCoordinateGrid(pdf);

        assertFalse(pdf.selectPaths().isEmpty(), "Should have drawn coordinate grid paths");
        assertFalse(pdf.page(0).selectParagraphsStartingWith("TOP-LEFT").isEmpty(), "Missing TOP-LEFT marker");
        assertFalse(pdf.page(0).selectParagraphsStartingWith("CENTER").isEmpty(), "Missing CENTER marker");
        assertFalse(pdf.page(0).selectParagraphsStartingWith("50").isEmpty(), "Missing numeric labels");

        pdf.save("/tmp/test-util-coordinate-grid.pdf");
    }

    @Test
    public void testDrawCoordinateGridOnSpecificPage() throws IOException {
        PDFDancer pdf = newPdf();

        TestUtil.drawCoordinateGrid(pdf, 0);

        assertFalse(pdf.selectPaths().isEmpty(), "Should have drawn coordinate grid paths");
        assertFalse(pdf.page(0).selectParagraphsStartingWith("BOTTOM-RIGHT").isEmpty(), "Missing BOTTOM-RIGHT marker");

        pdf.save("/tmp/test-util-coordinate-grid-page0.pdf");
    }
}
