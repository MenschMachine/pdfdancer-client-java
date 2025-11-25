package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility methods for test classes.
 */
public class TestUtil {

    public static void assertBetween(int lower, int upper, int value) {
        assertTrue(value >= lower && value <= upper,
                String.format("Expected value between %d and %d but got %d", lower, upper, value));
    }

    /**
     * Draws a coordinate grid on the specified page of a PDF document.
     * The grid includes:
     * - X and Y axes from origin (0,0)
     * - Major tick marks every 50 points with numeric labels
     * - Fine grid lines every 10 points in light gray
     * - Corner and center text markers
     *
     * @param pdf       the PDFDancer instance to draw on
     * @param pageNumber the page number (1-based) to draw the grid on
     */
    public static void drawCoordinateGrid(PDFDancer pdf, int pageNumber) {
        double width = pdf.getPages().get(pageNumber - 1).getPageSize().getWidth();
        double height = pdf.getPages().get(pageNumber - 1).getPageSize().getHeight();

        // Axes (origin at 0,0)
        pdf.page(pageNumber).newLine().from(0, 0).to(width, 0).color(Color.BLACK).lineWidth(2.0).add(); // X-axis
        pdf.page(pageNumber).newLine().from(0, 0).to(0, height).color(Color.BLACK).lineWidth(2.0).add(); // Y-axis

        double majorStep = 50.0;  // major tick spacing
        double tickLen = 10.0;    // tick mark length

        // X-axis major ticks and labels
        for (double x = 0.0; x <= width; x += majorStep) {
            pdf.page(pageNumber).newLine()
                    .from(x, 0)
                    .to(x, tickLen)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();

            pdf.newParagraph()
                    .text(String.valueOf((int) x))
                    .font("Courier", 6)
                    .color(Color.BLACK)
                    .at(pageNumber, x + 2, tickLen + 6)
                    .add();
        }

        // Y-axis major ticks and labels
        for (double y = 0.0; y <= height; y += majorStep) {
            pdf.page(pageNumber).newLine()
                    .from(0, y)
                    .to(tickLen, y)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();

            pdf.newParagraph()
                    .text(String.valueOf((int) y))
                    .font("Courier", 6)
                    .color(Color.BLACK)
                    .at(pageNumber, tickLen + 6, y + 2)
                    .add();
        }

        // Fine grid lines (10pt spacing, light gray)
        double gridStep = 10.0;
        Color gridColor = new Color(180, 180, 180, 51); // light gray with ~20% opacity

        // Vertical grid lines
        for (double gx = gridStep; gx <= width; gx += gridStep) {
            pdf.page(pageNumber).newLine()
                    .from(gx, 0)
                    .to(gx, height)
                    .color(gridColor)
                    .lineWidth(0.25)
                    .add();
        }

        // Horizontal grid lines
        for (double gy = gridStep; gy <= height; gy += gridStep) {
            pdf.page(pageNumber).newLine()
                    .from(0, gy)
                    .to(width, gy)
                    .color(gridColor)
                    .lineWidth(0.25)
                    .add();
        }

        // Corner and center text markers
        pdf.newParagraph().text("TOP-LEFT").font("Courier", 9).color(new Color(200, 0, 0))
                .at(pageNumber, 4, height - 12).add();
        pdf.newParagraph().text("TOP-RIGHT").font("Courier", 9).color(new Color(0, 120, 200))
                .at(pageNumber, Math.max(0, width - 100), height - 12).add();
        pdf.newParagraph().text("BOTTOM-LEFT").font("Courier", 9).color(new Color(0, 160, 0))
                .at(pageNumber, 4, 4).add();
        pdf.newParagraph().text("BOTTOM-RIGHT").font("Courier", 9).color(new Color(120, 0, 160))
                .at(pageNumber, Math.max(0, width - 110), 4).add();
        pdf.newParagraph().text("CENTER").font("Courier", 10).color(Color.BLACK)
                .at(pageNumber, width / 2, height / 2).add();
    }

    /**
     * Draws a coordinate grid on page 1 of the PDF document.
     *
     * @param pdf the PDFDancer instance to draw on
     */
    public static void drawCoordinateGrid(PDFDancer pdf) {
        drawCoordinateGrid(pdf, 1);
    }
}
