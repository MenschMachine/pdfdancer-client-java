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
     * - Major tick marks every 50 points
     * - Fine grid lines every 10 points in light gray
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

        // X-axis major ticks
        for (double x = 0.0; x <= width; x += majorStep) {
            pdf.page(pageNumber).newLine()
                    .from(x, 0)
                    .to(x, tickLen)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
                    .add();
        }

        // Y-axis major ticks
        for (double y = 0.0; y <= height; y += majorStep) {
            pdf.page(pageNumber).newLine()
                    .from(0, y)
                    .to(tickLen, y)
                    .color(Color.BLACK)
                    .lineWidth(0.5)
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
