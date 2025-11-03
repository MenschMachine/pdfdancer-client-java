package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.Color;
import com.tfc.pdf.pdfdancer.api.common.model.PageRef;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateSystemTest extends BaseTest {

    @Test
    public void drawCoordinateSystem() throws IOException {
        PDFDancer pdf = newPdf();

        PageRef page = pdf.getPages().get(0);
        double width = page.getPageSize().getWidth();
        double height = page.getPageSize().getHeight();

        // Axes (origin at 0,0; no margins)
        pdf.page(0).newLine().from(0, 0).to(width, 0).color(Color.BLACK).lineWidth(2.0).add(); // X-axis
        pdf.page(0).newLine().from(0, 0).to(0, height).color(Color.BLACK).lineWidth(2.0).add(); // Y-axis

        double step = 50.0;      // scale step in points
        double tickLen = 10.0;   // shorter tick length

        // X-axis ticks and labels (vertical ticks up from y=0)
        for (double x = 0.0; x <= width; x += step) {
            pdf.page(0).newLine()
                    .from(x, 0)
                    .to(x, tickLen)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();

            // Numeric label above the tick
            pdf.newParagraph()
                    .text(String.valueOf((int) x))
                    .font("Courier", 6)
                    .color(Color.BLACK)
                    .at(0, x + 2, tickLen + 6)
                    .add();
        }

        // Y-axis ticks and labels (horizontal ticks right from x=0)
        for (double y = 0.0; y <= height; y += step) {
            pdf.page(0).newLine()
                    .from(0, y)
                    .to(tickLen, y)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();

            // Numeric label right of the tick
            pdf.newParagraph()
                    .text(String.valueOf((int) y))
                    .font("Courier", 6)
                    .color(Color.BLACK)
                    .at(0, tickLen + 6, y + 2)
                    .add();
        }
        // Very tiny coordinate grid lines (faint), ~10pt spacing, ~20% opacity if supported
        double gridStep = 10.0;
        Color gridColor = new Color(180, 180, 180, 51); // light gray, alpha=~20%
        // Vertical grid lines (skip x=0 axis)
        for (double gx = gridStep; gx <= width; gx += gridStep) {
            pdf.page(0).newLine()
                    .from(gx, 0)
                    .to(gx, height)
                    .color(gridColor)
                    .lineWidth(0.25)
                    .add();
        }
        // Horizontal grid lines (skip y=0 axis)
        for (double gy = gridStep; gy <= height; gy += gridStep) {
            pdf.page(0).newLine()
                    .from(0, gy)
                    .to(width, gy)
                    .color(gridColor)
                    .lineWidth(0.25)
                    .add();
        }

        // -- Text markers at key positions --
        pdf.newParagraph().text("TOP-LEFT").font("Courier", 9).color(new Color(200,0,0))
                .at(0, 4, height - 12).add();
        pdf.newParagraph().text("TOP-RIGHT").font("Courier", 9).color(new Color(0,120,200))
                .at(0, Math.max(0, width - 100), height - 12).add();
        pdf.newParagraph().text("BOTTOM-LEFT").font("Courier", 9).color(new Color(0,160,0))
                .at(0, 4, 4).add();
        pdf.newParagraph().text("BOTTOM-RIGHT").font("Courier", 9).color(new Color(120,0,160))
                .at(0, Math.max(0, width - 110), 4).add();
        pdf.newParagraph().text("CENTER").font("Courier", 10).color(Color.BLACK)
                .at(0, width/2, height/2).add();

        // -- Images at various positions (80x80 logo) --
        File logo = new File("src/test/resources/fixtures/logo-80.png");
        // Top-left (slightly inset so fully visible)
        pdf.newImage().fromFile(logo).at(0, 10, Math.max(0, height - 90)).add();
        // Top-right
        pdf.newImage().fromFile(logo).at(0, Math.max(0, width - 90), Math.max(0, height - 90)).add();
        // Bottom-left
        pdf.newImage().fromFile(logo).at(0, 10, 10).add();
        // Bottom-right
        pdf.newImage().fromFile(logo).at(0, Math.max(0, width - 90), 10).add();
        // Center
        pdf.newImage().fromFile(logo).at(0, Math.max(0, width/2 - 40), Math.max(0, height/2 - 40)).add();

        // Spot-check text and image placements
        assertFalse(pdf.page(0).selectParagraphsStartingWith("TOP-LEFT").isEmpty(), "Missing TOP-LEFT text");
        assertFalse(pdf.page(0).selectParagraphsStartingWith("CENTER").isEmpty(), "Missing CENTER text");
        // Images: count
        assertEquals(5, pdf.page(0).selectImages().size(), "Expected 5 images at corners and center");


        // Basic assertions: some paths exist and representative ticks and grid are present
        assertFalse(pdf.selectPaths().isEmpty(), "Should have drawn some paths");
        assertFalse(pdf.page(0).selectPathAt(50, 0).isEmpty(), "Missing X tick at x=50");
        assertFalse(pdf.page(0).selectPathAt(0, 50).isEmpty(), "Missing Y tick at y=50");
        assertFalse(pdf.page(0).selectPathAt(10, 10).isEmpty(), "Missing grid intersection at (10,10)");
        assertFalse(pdf.page(0).selectParagraphsStartingWith("50").isEmpty(), "Missing '50' label(s)");

        // Save for visual inspection
        pdf.save("/tmp/coordinate-system.pdf");
    }
}

