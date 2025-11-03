package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.Color;
import com.tfc.pdf.pdfdancer.api.common.response.PageSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathBuilderShowcaseTest extends BaseTest {

    @Test
    public void showcasePathBuilderWithFillAndMultiSegment() {
        PDFDancer pdf = newPdf();

        PageSnapshot snapshot = pdf.getPageSnapshot(0);
        double width = snapshot.pageRef().getPageSize().getWidth();
        double height = snapshot.pageRef().getPageSize().getHeight();

        double cx = width / 2.0;
        double cy = height / 2.0;
        double r = Math.min(width, height) * 0.18;
        double k = 0.5522847498 * r; // circle kappa

        // Multi-segment Bezier circle in ONE path, with fill + stroke
        pdf.page(0).newPath()
                .color(Color.BLACK)
                .fillColor(new Color(220, 20, 60, 160))
                .lineWidth(2.0)
                .moveTo(cx, cy + r)
                .bezierTo(cx + k, cy + r, cx + r, cy + k, cx + r, cy)
                .bezierTo(cx + r, cy - k, cx + k, cy - r, cx, cy - r)
                .bezierTo(cx - k, cy - r, cx - r, cy - k, cx - r, cy)
                .bezierTo(cx - r, cy + k, cx - k, cy + r, cx, cy + r)
                .add();

        // Multi-segment S-curve (stroke only) using two cubic segments
        double left = 40;
        double right = width - 40;
        double y1 = height - 120;
        double y2 = height - 200;
        pdf.page(0).newPath()
                .color(new Color(30, 144, 255))
                .lineWidth(2.5)
                .moveTo(left, y1)
                .bezierTo(left + (right - left) * 0.25, y1 + 40, left + (right - left) * 0.25, y1 - 40, left + (right - left) * 0.50, y1)
                .bezierTo(left + (right - left) * 0.75, y1 + 40, left + (right - left) * 0.75, y2 - 40, right, y2)
                .add();

        // Assertions
        assertEquals(2, pdf.selectPaths().size(), "Two paths should be present");

        pdf.save("/tmp/pathbuilder-showcase.pdf");
    }
}

