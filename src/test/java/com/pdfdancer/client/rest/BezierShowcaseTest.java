package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.response.PageSnapshot;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BezierShowcaseTest extends BaseTest {

    @Test
    public void showcaseBezierCurves() throws IOException {
        PDFDancer pdf = newPdf();

        PageSnapshot snapshot = pdf.getPageSnapshot(0);
        double width = snapshot.pageRef().getPageSize().getWidth();
        double height = snapshot.pageRef().getPageSize().getHeight();

        double margin = 40;
        double left = margin;
        double right = width - margin;
        double top = height - margin;
        double bottom = margin;

        // 1) Simple S-curve across the page
        pdf.page(0).newBezier()
                .from(left, top - 80)
                .control1(left + (right - left) * 0.25, top - 20)
                .control2(left + (right - left) * 0.75, top - 140)
                .to(right, top - 80)
                .color(new Color(30, 144, 255))
                .lineWidth(2)
                .add();

        // 2) Gentle arc
        pdf.page(0).newBezier()
                .from(left, top - 140)
                .control1(left + (right - left) * 0.33, top - 140)
                .control2(left + (right - left) * 0.66, top - 220)
                .to(right, top - 220)
                .color(new Color(220, 20, 60))
                .lineWidth(1.5)
                .add();

        // 3) Tight curve with thicker stroke
        pdf.page(0).newBezier()
                .from(left + 20, top - 260)
                .control1(left + 60, top - 260)
                .control2(left + 140, top - 320)
                .to(left + 220, top - 320)
                .color(Color.BLACK)
                .lineWidth(4)
                .add();

        // 4) Dashed curve
        pdf.page(0).newBezier()
                .from(left, top - 380)
                .control1(left + (right - left) * 0.25, top - 320)
                .control2(left + (right - left) * 0.75, top - 440)
                .to(right, top - 380)
                .color(new Color(34, 139, 34))
                .lineWidth(2)
                .dash(6, 3)
                .add();

        // 5) Dashed curve with phase offset
        pdf.page(0).newBezier()
                .from(left, top - 440)
                .control1(left + (right - left) * 0.25, top - 380)
                .control2(left + (right - left) * 0.75, top - 500)
                .to(right, top - 440)
                .color(new Color(128, 0, 128))
                .lineWidth(2)
                .dashWithPhase(4, 6, 3)
                .add();

        // 6-9) Four-quarter circle approximation using 4 Beziers (classic kappa ~0.5522847498)
        double cx = width / 2.0;
        double cy = height / 2.0;
        double r = Math.min(width, height) * 0.18;
        double k = 0.5522847498 * r;
        // Top-right quarter
        pdf.page(0).newBezier()
                .from(cx, cy + r)
                .control1(cx + k, cy + r)
                .control2(cx + r, cy + k)
                .to(cx + r, cy)
                .color(new Color(0, 0, 0, 200))
                .lineWidth(1.75)
                .add();
        // Right-bottom quarter (dashed)
        pdf.page(0).newBezier()
                .from(cx + r, cy)
                .control1(cx + r, cy - k)
                .control2(cx + k, cy - r)
                .to(cx, cy - r)
                .color(new Color(0, 0, 0, 200))
                .lineWidth(1.75)
                .dash(3, 2)
                .add();
        // Bottom-left quarter
        pdf.page(0).newBezier()
                .from(cx, cy - r)
                .control1(cx - k, cy - r)
                .control2(cx - r, cy - k)
                .to(cx - r, cy)
                .color(new Color(0, 0, 0, 200))
                .lineWidth(1.75)
                .add();
        // Left-top quarter (dashed with phase)
        pdf.page(0).newBezier()
                .from(cx - r, cy)
                .control1(cx - r, cy + k)
                .control2(cx - k, cy + r)
                .to(cx, cy + r)
                .color(new Color(0, 0, 0, 200))
                .lineWidth(1.75)
                .dashWithPhase(1, 3, 2)
                .add();

        // Verify we added the expected number of path objects
        int expectedCurves = 9; // 5 single curves + 4 for the circle
        assertEquals(expectedCurves, pdf.selectPaths().size());

        // Spot-check a few positions (hit-test near curve start points)
        assertFalse(pdf.page(0).selectPathsAt(left, top - 80).isEmpty());
        assertFalse(pdf.page(0).selectPathsAt(left, top - 380).isEmpty());
        assertFalse(pdf.page(0).selectPathsAt(cx, cy + r).isEmpty());

        // Save for visual inspection
        pdf.save("/tmp/bezier-Showcase.pdf");
    }
}

