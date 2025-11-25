package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.response.PageSnapshot;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LineShowcaseTest extends BaseTest {

    @Test
    public void showcaseLineStyles() throws IOException {
        PDFDancer pdf = newPdf();

        PageSnapshot snapshot = pdf.getPageSnapshot(1);
        double width = snapshot.pageRef().getPageSize().getWidth();
        double height = snapshot.pageRef().getPageSize().getHeight();

        double left = 50;
        double right = width - 50;
        double y = height - 60; // start near top
        double step = 24;

        // Solid lines with varying widths
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(0.25).add();
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(1).add();
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(2).add();
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(5).add();
        y -= step;

        // Colored lines
        pdf.page(1).newLine().from(left, y).to(right, y).color(new Color(220, 20, 60)).lineWidth(2).add(); // crimson
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(new Color(30, 144, 255)).lineWidth(2).add(); // dodger blue
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(new Color(34, 139, 34)).lineWidth(2).add(); // forest green
        y -= step;

        // Semi-transparent line
        pdf.page(1).newLine().from(left, y).to(right, y).color(new Color(0, 0, 0, 64)).lineWidth(3).add();
        y -= step;

        // Dashed patterns
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(1).dash(2, 2).add(); // short dash
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(1.5).dash(4, 4).add(); // medium dash
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(1).dash(6, 3).add(); // long dash
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(Color.BLACK).lineWidth(0.75).dash(1, 2).add(); // dotted-like
        y -= step;

        // Same dash with phase to show offset
        pdf.page(1).newLine().from(left, y).to(right, y).color(new Color(128, 0, 128)).lineWidth(1.25).dash(6, 3).add();
        y -= step;
        pdf.page(1).newLine().from(left, y).to(right, y).color(new Color(128, 0, 128)).lineWidth(1.25).dashWithPhase(3, 6, 3).add();
        y -= step;

        // Vertical dashed line
        pdf.page(1).newLine().from(left + 50, 50).to(left + 50, height - 50).color(Color.BLACK).lineWidth(1).dash(3, 3).add();

        // Diagonal dashed line
        pdf.page(1).newLine().from(left, 50).to(right, height - 250).color(new Color(90, 90, 90)).lineWidth(1).dash(2, 6).add();

        // Verify we added the expected number of path objects
        int expectedLines = 16;
        assertEquals(expectedLines, pdf.selectPaths().size());

        // Spot-check a few positions (hit-test at line starts)
        assertFalse(pdf.page(1).selectPathsAt(left, height - 60).isEmpty()); // first solid
        assertFalse(pdf.page(1).selectPathsAt(left, height - 60 - (4 * step)).isEmpty()); // after 4 solids
        assertFalse(pdf.page(1).selectPathsAt(left + 50, 50).isEmpty()); // vertical line start

        // Save for visual inspection
        pdf.save("/tmp/line-Showcase.pdf");
    }
}

