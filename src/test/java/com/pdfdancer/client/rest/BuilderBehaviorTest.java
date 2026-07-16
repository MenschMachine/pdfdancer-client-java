package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuilderBehaviorTest extends BaseTest {

    @Test
    void everyDedicatedBuilderWorksAtDocumentAndPageScope() {
        PDFDancer pdf = newPdf();

        assertTrue(pdf.newLine(1).from(20, 20).to(120, 20).color(Color.RED).lineWidth(2).add());
        assertTrue(pdf.page(1).newLine().from(20, 40).to(120, 40).dash(4, 2).add());

        assertTrue(pdf.newBezier(1).from(20, 80).control1(50, 120).control2(90, 40).to(120, 80).add());
        assertTrue(pdf.page(1).newBezier().from(20, 100).control1(50, 140).control2(90, 60).to(120, 100)
                .fillColor(new Color(200, 220, 255, 128)).evenOddFill(true).add());

        assertTrue(pdf.newRectangle(1).at(150, 20).size(80, 40).color(Color.BLACK).add());
        assertTrue(pdf.page(1).newRectangle().at(150, 80).size(80, 40)
                .fillColor(new Color(255, 220, 200)).dashWithPhase(2, 4, 2).add());

        assertEquals(6, pdf.selectPaths().size());
        new PDFAssertions(pdf).assertNumberOfPaths(6, 1);
    }

    @Test
    void pathConveniencesPersistLinesBezierRectanglesCirclesAndClosedSubpaths() {
        PDFDancer pdf = newPdf();

        assertTrue(pdf.newPath(1)
                .moveTo(20, 20).lineTo(100, 20)
                .bezierTo(120, 20, 120, 80, 100, 80)
                .lineTo(20, 80).closePath()
                .fillColor(new Color(255, 0, 0, 80)).dash(5, 2).solid().add());
        assertTrue(pdf.page(1).newPath().rect(140, 20, 60, 40).add());
        assertTrue(pdf.page(1).newPath().circle(250, 60, 30).evenOddFill(true).add());

        new PDFAssertions(pdf).assertNumberOfPaths(3, 1);
    }

    @Test
    void dedicatedBuildersRejectMissingRequiredGeometry() {
        PDFDancer pdf = newPdf();

        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newLine().to(10, 10).add());
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newLine().from(0, 0).add());
        assertThrows(IllegalArgumentException.class,
                () -> pdf.page(1).newBezier().from(0, 0).control1(1, 1).control2(2, 2).add());
        assertThrows(IllegalStateException.class, () -> pdf.page(1).newRectangle().add());
    }

    @Test
    void buildersRejectInvalidDimensionsWidthsCoordinatesAndDashPatterns() {
        PDFDancer pdf = newPdf();

        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newRectangle().at(0, 0).size(0, 10));
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newRectangle().at(0, 0).size(10, -1));
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newPath().lineWidth(-0.1));
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newPath().moveTo(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newPath().dash(0, 0));
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newPath().circle(20, 20, 0));
    }

    @Test
    void lineBuilderRejectsNegativeWidth() {
        PDFDancer pdf = newPdf();
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newLine().lineWidth(-0.1));
    }

    @Test
    void bezierBuilderRejectsNegativeWidth() {
        PDFDancer pdf = newPdf();
        assertThrows(IllegalArgumentException.class, () -> pdf.page(1).newBezier().lineWidth(-0.1));
    }

    @Test
    void dedicatedBuildersRejectNonfiniteWidths() {
        PDFDancer pdf = newPdf();
        assertThrows(IllegalArgumentException.class,
                () -> pdf.page(1).newLine().lineWidth(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> pdf.page(1).newLine().lineWidth(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> pdf.page(1).newBezier().lineWidth(Double.NEGATIVE_INFINITY));
    }
}
