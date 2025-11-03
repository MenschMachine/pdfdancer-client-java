package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.Color;
import com.tfc.pdf.pdfdancer.api.common.response.PageSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathConvenienceMethodsTest extends BaseTest {

    @Test
    public void convenienceCircleRectAndClosePath() {
        PDFDancer pdf = newPdf();

        PageSnapshot snapshot = pdf.getPageSnapshot(0);
        double width = snapshot.pageRef().getPageSize().getWidth();
        double height = snapshot.pageRef().getPageSize().getHeight();

        // Rectangle at bottom-left
        pdf.page(0).newPath()
                .color(Color.BLACK)
                .fillColor(new Color(50, 205, 50, 140))
                .lineWidth(1.5)
                .rect(40, 40, 120, 80)
                .add();

        // Circle in the center
        double r = Math.min(width, height) * 0.12;
        pdf.page(0).newPath()
                .color(new Color(25, 25, 112))
                .fillColor(new Color(220, 20, 60, 160))
                .lineWidth(2.0)
                .circle(width / 2.0, height / 2.0, r)
                .add();

        // Triangle via lines, then closePath (stroke only)
        double tx = width - 160;
        double ty = height - 200;
        pdf.page(0).newPath()
                .color(new Color(30, 144, 255))
                .lineWidth(2.0)
                .moveTo(tx, ty)
                .lineTo(tx + 120, ty)
                .lineTo(tx + 60, ty + 100)
                .closePath()
                .add();

        assertEquals(3, pdf.selectPaths().size(), "Three paths should be present");
        pdf.save("/tmp/path-convenience.pdf");
    }
}

