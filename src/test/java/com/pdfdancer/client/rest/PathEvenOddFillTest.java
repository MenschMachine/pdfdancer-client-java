package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathEvenOddFillTest extends BaseTest {

    @Test
    public void selfIntersectingPolygonWithEvenOddFill() {
        PDFDancer pdf = newPdf();

        // Draw a simple bow-tie (self-intersecting) polygon; even-odd fill should differ visually from nonzero
        pdf.page(0).newPath()
                .fillColor(new Color(200, 50, 50, 180))
                .color(new Color(20, 20, 20))
                .lineWidth(1.5)
                .evenOddFill(true)
                .moveTo(200, 200)
                .lineTo(350, 350)
                .lineTo(200, 350)
                .lineTo(350, 200)
                .closePath()
                .add();

        assertEquals(1, pdf.selectPaths().size(), "One path should be present");
        pdf.save("/tmp/path-evenodd.pdf");
    }
}

