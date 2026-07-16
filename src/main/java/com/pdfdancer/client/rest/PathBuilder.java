package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Point;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.path.Bezier;
import com.pdfdancer.common.model.path.Line;
import com.pdfdancer.common.model.path.Path;
import com.pdfdancer.common.model.path.PathSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing a multi-segment Path (lines and cubic Beziers)
 * and adding it to a page as a single vector object.
 */
public class PathBuilder {

    private final PDFDancer client;
    private final int pageNumber;

    private final List<PathSegment> segments = new ArrayList<>();
    private Point current; // cursor

    private Point start; // start of current subpath

    // styling applies to all newly added segments unless changed
    private Color strokeColor = Color.BLACK;
    private Color fillColor;
    private Double strokeWidth = 1.0;
    private double[] dashArray;
    private Double dashPhase;
    private Boolean evenOddFill; // null -> default (nonzero)

    PathBuilder(PDFDancer client, int pageNumber) {
        if (pageNumber < 1) throw new IllegalArgumentException("Page number must be >= 1");
        this.client = client;
        this.pageNumber = pageNumber;
    }

    public PathBuilder moveTo(double x, double y) {
        requireFinite(x, y);
        this.current = new Point(x, y);
        this.start = this.current;
        return this;
    }

    public PathBuilder lineTo(double x, double y) {
        if (current == null) throw new IllegalStateException("Call moveTo() before lineTo()");
        requireFinite(x, y);
        Point next = new Point(x, y);
        Line line = new Line(current, next);
        applyStyle(line);
        // position anchored at the segment's starting point
        line.setPosition(Position.atPageCoordinates(pageNumber, current.x(), current.y()));
        segments.add(line);
        this.current = next;
        return this;
    }

    public PathBuilder bezierTo(double cx1, double cy1, double cx2, double cy2, double x, double y) {
        if (current == null) throw new IllegalStateException("Call moveTo() before bezierTo()");
        requireFinite(cx1, cy1, cx2, cy2, x, y);
        Point c1 = new Point(cx1, cy1);
        Point c2 = new Point(cx2, cy2);
        Point end = new Point(x, y);
        Bezier b = new Bezier(current, c1, c2, end);
        applyStyle(b);
        b.setPosition(Position.atPageCoordinates(pageNumber, current.x(), current.y()));
        segments.add(b);
        this.current = end;
        return this;
    }

    public PathBuilder color(Color color) {
        this.strokeColor = color;
        return this;
    }

    public PathBuilder fillColor(Color color) {
        this.fillColor = color;
        return this;
    }

    public PathBuilder lineWidth(double width) {
        if (!Double.isFinite(width) || width < 0) {
            throw new IllegalArgumentException("Line width must be finite and nonnegative");
        }
        this.strokeWidth = width;
        return this;
    }

    public PathBuilder dash(double... pattern) {
        validateDash(0, pattern);
        this.dashArray = pattern;
        this.dashPhase = 0.0;
        return this;
    }

    public PathBuilder dashWithPhase(double phase, double... pattern) {
        validateDash(phase, pattern);
        this.dashArray = pattern;
        this.dashPhase = phase;
        return this;
    }

    public PathBuilder solid() {
        this.dashArray = null;
        this.dashPhase = null;
        return this;
    }

    public PathBuilder addSegment(PathSegment segment) {
        if (segment == null) throw new IllegalArgumentException("Path segment must not be null");
        applyStyle(segment);
        if (segment.getPosition() == null) segment.setPosition(Position.atPage(pageNumber));
        segments.add(segment);
        return this;
    }

    public PathBuilder evenOddFill(boolean evenOdd) {
        this.evenOddFill = evenOdd;
        return this;
    }

    public boolean add() {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No segments in path. Use moveTo()/lineTo()/bezierTo() to add segments.");
        }
        Path path = new Path();
        if (evenOddFill != null) {
            path.setEvenOddFill(evenOddFill);
        }
        for (PathSegment s : segments) {
            path.addPathSegment(s);
        }
        return client.addObject(path);
    }

    private void applyStyle(PathSegment seg) {
        if (strokeColor != null) seg.setStrokeColor(strokeColor);
        if (fillColor != null) seg.setFillColor(fillColor);
        if (strokeWidth != null) seg.setStrokeWidth(strokeWidth);
        if (dashArray != null && dashArray.length > 0) seg.setDashArray(dashArray);
        if (dashPhase != null) seg.setDashPhase(dashPhase);
    }

    /**
     * Closes the current subpath by connecting the current point back to the last moveTo point.
     * If already at the start, no extra segment is added.
     */
    public PathBuilder closePath() {
        if (current == null || start == null) {
            throw new IllegalStateException("Call moveTo() before closePath()");
        }
        if (current.x() != start.x() || current.y() != start.y()) {
            // add a closing line
            lineTo(start.x(), start.y());
        }
        // keep current at start for potential continued paths
        this.current = start;
        return this;
    }

    /**
     * Adds a rectangle path starting at bottom-left (x, y) with given width and height.
     * Coordinates are in points; origin is bottom-left of the page.
     */
    public PathBuilder rect(double x, double y, double width, double height) {
        requireFinite(x, y, width, height);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Rectangle width and height must be positive");
        }
        moveTo(x, y);
        lineTo(x + width, y);
        lineTo(x + width, y + height);
        lineTo(x, y + height);
        return closePath();
    }

    /**
     * Adds a circle approximation using four cubic Bezier segments.
     * Center at (cx, cy), radius r, using kappa approximation.
     */
    public PathBuilder circle(double cx, double cy, double r) {
        requireFinite(cx, cy, r);
        if (r <= 0) throw new IllegalArgumentException("Circle radius must be positive");
        final double k = 0.5522847498 * r;
        moveTo(cx, cy + r);
        bezierTo(cx + k, cy + r, cx + r, cy + k, cx + r, cy);
        bezierTo(cx + r, cy - k, cx + k, cy - r, cx, cy - r);
        bezierTo(cx - k, cy - r, cx - r, cy - k, cx - r, cy);
        bezierTo(cx - r, cy + k, cx - k, cy + r, cx, cy + r);
        // curve ends back at start; close does nothing if already closed
        return closePath();
    }

    private static void requireFinite(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Coordinates must be finite");
        }
    }

    private static void validateDash(double phase, double... pattern) {
        if (!Double.isFinite(phase) || phase < 0) {
            throw new IllegalArgumentException("Dash phase must be finite and nonnegative");
        }
        if (pattern == null) throw new IllegalArgumentException("Dash pattern must not be null");
        boolean anyPositive = pattern.length == 0;
        for (double value : pattern) {
            if (!Double.isFinite(value) || value < 0) {
                throw new IllegalArgumentException("Dash values must be finite and nonnegative");
            }
            anyPositive |= value > 0;
        }
        if (!anyPositive) throw new IllegalArgumentException("Dash pattern cannot be all zero");
    }
}
