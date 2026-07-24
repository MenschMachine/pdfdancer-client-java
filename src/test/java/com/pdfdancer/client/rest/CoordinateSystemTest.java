package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.PageRef;
import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.request.TextInsertRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CoordinateSystemTest extends BaseTest {

    @Test
    public void drawCoordinateSystem() throws IOException {
        PDFDancer pdf = newPdf();

        PageRef page = pdf.getPages().get(0);
        double width = page.getPageSize().getWidth();
        double height = page.getPageSize().getHeight();

        // Axes (origin at 0,0; no margins)
        pdf.page(1).newLine().from(0, 0).to(width, 0).color(Color.BLACK).lineWidth(2.0).add(); // X-axis
        pdf.page(1).newLine().from(0, 0).to(0, height).color(Color.BLACK).lineWidth(2.0).add(); // Y-axis

        double step = 50.0;      // scale step in points
        double tickLen = 10.0;   // shorter tick length

        // X-axis ticks and labels (vertical ticks up from y=0)
        for (double x = 0.0; x <= width; x += step) {
            pdf.page(1).newLine()
                    .from(x, 0)
                    .to(x, tickLen)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();

            addTextLabel(pdf, String.valueOf((int) x), x + 2, tickLen + 6, 6, PdfColorRequest.gray(0));
        }

        // Y-axis ticks and labels (horizontal ticks right from x=0)
        for (double y = 0.0; y <= height; y += step) {
            pdf.page(1).newLine()
                    .from(0, y)
                    .to(tickLen, y)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();

            addTextLabel(pdf, String.valueOf((int) y), tickLen + 6, y + 2, 6, PdfColorRequest.gray(0));
        }
        // Very tiny coordinate grid lines (faint), ~10pt spacing, ~20% opacity if supported
        double gridStep = 10.0;
        Color gridColor = new Color(180, 180, 180, 51); // light gray, alpha=~20%
        // Vertical grid lines (skip x=0 axis)
        for (double gx = gridStep; gx <= width; gx += gridStep) {
            pdf.page(1).newLine()
                    .from(gx, 0)
                    .to(gx, height)
                    .color(gridColor)
                    .lineWidth(0.25)
                    .add();
        }
        // Horizontal grid lines (skip y=0 axis)
        for (double gy = gridStep; gy <= height; gy += gridStep) {
            pdf.page(1).newLine()
                    .from(0, gy)
                    .to(width, gy)
                    .color(gridColor)
                    .lineWidth(0.25)
                    .add();
        }

        // -- Text markers at key positions --
        addTextLabel(pdf, "TOP-LEFT", 4, height - 12, 9, PdfColorRequest.rgb(0.78, 0, 0));
        addTextLabel(pdf, "TOP-RIGHT", Math.max(0, width - 100), height - 12, 9, PdfColorRequest.rgb(0, 0.47, 0.78));
        addTextLabel(pdf, "BOTTOM-LEFT", 4, 4, 9, PdfColorRequest.rgb(0, 0.63, 0));
        addTextLabel(pdf, "BOTTOM-RIGHT", Math.max(0, width - 110), 4, 9, PdfColorRequest.rgb(0.47, 0, 0.63));
        addTextLabel(pdf, "CENTER", width / 2, height / 2, 10, PdfColorRequest.gray(0));

        // -- Images at various positions (80x80 logo) --
        File logo = new File("src/test/resources/fixtures/logo-80.png");
        // Top-left (slightly inset so fully visible)
        pdf.newImage().fromFile(logo).at(1, 10, Math.max(0, height - 90)).add();
        // Top-right
        pdf.newImage().fromFile(logo).at(1, Math.max(0, width - 90), Math.max(0, height - 90)).add();
        // Bottom-left
        pdf.newImage().fromFile(logo).at(1, 10, 10).add();
        // Bottom-right
        pdf.newImage().fromFile(logo).at(1, Math.max(0, width - 90), 10).add();
        // Center
        pdf.newImage().fromFile(logo).at(1, Math.max(0, width / 2 - 40), Math.max(0, height / 2 - 40)).add();

        new PDFAssertions(pdf)
                .assertPdfTextContains("TOP-LEFT")
                .assertPdfTextContains("TOP-RIGHT")
                .assertPdfTextContains("BOTTOM-LEFT")
                .assertPdfTextContains("BOTTOM-RIGHT")
                .assertPdfTextContains("CENTER")
                .assertPdfTextContains("50");
        assertEquals(5, pdf.page(1).selectImages().size(), "Expected 5 images at corners and center");


        // Basic assertions: some paths exist and representative ticks and grid are present
        assertFalse(pdf.selectPaths().isEmpty(), "Should have drawn some paths");
        assertFalse(pdf.page(1).selectPathsAt(50, 0).isEmpty(), "Missing X tick at x=50");
        assertFalse(pdf.page(1).selectPathsAt(0, 50).isEmpty(), "Missing Y tick at y=50");
        assertFalse(pdf.page(1).selectPathsAt(10, 10).isEmpty(), "Missing grid intersection at (10,10)");

        // Save for visual inspection
        pdf.save("/tmp/coordinate-system.pdf");
    }

    private static void addTextLabel(PDFDancer pdf,
                                     String text,
                                     double x,
                                     double y,
                                     double size,
                                     PdfColorRequest color) {
        pdf.page(1).text().insert(TextInsertRequest.builder()
                .coordinate(x, y)
                .insert(text)
                .font("Courier")
                .size(size)
                .fillColor(color)
                .build());
    }
}
