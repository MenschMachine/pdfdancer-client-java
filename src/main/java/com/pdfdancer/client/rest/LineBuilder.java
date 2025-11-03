package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Point;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.path.Line;
import com.pdfdancer.common.model.path.Path;

/**
 * Fluent builder for adding a straight line to a PDF page.
 *
 * <p>Coordinate system: origin is at the bottom-left of the page, X to the right, Y upwards.
 * All distances are in points (1 point = 1/72 inch). The line is added as a one-segment
 * {@code Path} with STROKE painting.</p>
 *
 * <p>Styling options:</p>
 * <ul>
 *   <li>{@link #color(Color)} — stroke color (RGBA). Alpha &lt; 255 yields semi-transparency.</li>
 *   <li>{@link #lineWidth(double)} — stroke width in points. Width 0 produces a device-dependent
 *       hairline in PDF; use a small positive value (e.g., 0.25) for a thin but consistent line.</li>
 *   <li>{@link #dash(double...)} and {@link #dashWithPhase(double, double...)} — dash pattern and phase, in user space units.</li>
 * </ul>
 *
 * <p>Defaults: if color/width are not set explicitly, backend defaults apply (typically black, width 1.0).</p>
 *
 * <p>Thread-safety: instances are not thread-safe. Intended usage is single-threaded, typically as
 * part of a fluent chain per line.</p>
 */
public class LineBuilder {

    private final PDFDancer client;
    private final int pageIndex;
    private Point from;
    private Point to;
    private Color strokeColor;
    private Double strokeWidth;
    private double[] dashArray;
    private Double dashPhase;

    /**
     * Creates a builder bound to a specific page.
     *
     * @param client    PDFDancer client used to submit the object
     * @param pageIndex zero-based index of the page to draw the line on
     */
    public LineBuilder(PDFDancer client, int pageIndex) {
        this.client = client;
        this.pageIndex = pageIndex;
    }

    /**
     * Sets the start point of the line in page coordinates.
     *
     * <p>Units are points (1/72 inch). Origin at bottom-left.</p>
     *
     * @param x x-coordinate of the start point
     * @param y y-coordinate of the start point
     * @return this builder for chaining
     */
    public LineBuilder from(double x, double y) {
        this.from = new Point(x, y);
        return this;
    }

    /**
     * Sets the end point of the line in page coordinates.
     *
     * <p>Units are points (1/72 inch). Origin at bottom-left.</p>
     *
     * @param x x-coordinate of the end point
     * @param y y-coordinate of the end point
     * @return this builder for chaining
     */
    public LineBuilder to(double x, double y) {
        this.to = new Point(x, y);
        return this;
    }

    /**
     * Sets the stroke color.
     *
     * <p>Uses RGBA; alpha &lt; 255 yields semi-transparent strokes, depending on the
     * renderer. If not set, a backend default is used (typically black).</p>
     *
     * @param color stroke color (RGBA)
     * @return this builder for chaining
     */
    public LineBuilder color(Color color) {
        this.strokeColor = color;
        return this;
    }

    /**
     * Sets the stroke width in points.
     *
     * <p>A width of 0 produces a device-dependent hairline per PDF spec. For a
     * thin but consistent line, use a small positive value like 0.25.</p>
     *
     * @param width stroke width in points (non-negative)
     * @return this builder for chaining
     */
    public LineBuilder lineWidth(double width) {
        this.strokeWidth = width;
        return this;
    }

    /**
     * Sets a dash pattern for the stroke with zero phase (no offset).
     *
     * <p>The pattern is interpreted as alternating on/off lengths in user space units,
     * e.g., {@code (3, 2)} = 3pt on, 2pt off, {@code (10, 5, 2, 5)} = dash-dot-like.</p>
     * <p>Null or empty pattern means a solid line. Values should be non-negative
     * and the pattern should not be all zeros.</p>
     *
     * @param pattern dash array (on/off lengths), in points
     * @return this builder for chaining
     */
    public LineBuilder dash(double... pattern) {
        this.dashArray = pattern;
        this.dashPhase = 0.0;
        return this;
    }

    /**
     * Sets a dash pattern with an explicit phase (offset) into the pattern.
     *
     * <p>Phase is the distance into the pattern at which stroking begins. For a
     * pattern {@code (10, 5)} and {@code phase=5}, the first dash is shortened by 5.</p>
     *
     * @param phase   dash phase (offset) in points
     * @param pattern dash array (on/off lengths) in points
     * @return this builder for chaining
     */
    public LineBuilder dashWithPhase(double phase, double... pattern) {
        this.dashArray = pattern;
        this.dashPhase = phase;
        return this;
    }

    /**
     * Finalizes the line and adds it to the PDF as a single-segment Path.
     *
     * <p>Validation: both {@link #from(double, double)} and {@link #to(double, double)}
     * must be specified or an {@link IllegalArgumentException} is thrown.</p>
     *
     * <p>Effect: constructs a {@code Path} containing one {@code Line} segment with the
     * configured stroke color, width, and optional dash settings, positioned on the
     * specified page.</p>
     *
     * @return true if the object was accepted by the server
     * @throws IllegalArgumentException if start or end point is missing
     */
    public boolean add() {
        // Validation
        if (from == null) {
            throw new IllegalArgumentException("Line start point (from) is not set");
        }
        if (to == null) {
            throw new IllegalArgumentException("Line end point (to) is not set");
        }

        // Build the Line segment
        Line line = new Line(from, to);
        if (strokeColor != null) {
            line.setStrokeColor(strokeColor);
        }
        if (strokeWidth != null) {
            line.setStrokeWidth(strokeWidth);
        }
        // Dash pattern (if provided)
        if (dashArray != null && dashArray.length > 0) {
            line.setDashArray(dashArray);
        }
        if (dashPhase != null) {
            line.setDashPhase(dashPhase);
        }
        // Set position on the line segment (page index is what matters)
        line.setPosition(Position.atPageCoordinates(pageIndex, from.x(), from.y()));

        // Build the Path
        Path path = new Path();
        path.addPathSegment(line);

        // Add to PDF
        return client.addObject(path);
    }
}
