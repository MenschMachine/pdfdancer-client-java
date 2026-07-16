package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;

/** Fluent builder for an axis-aligned rectangle on one page. */
public final class RectangleBuilder {
    private final PathBuilder path;
    private Double x;
    private Double y;
    private Double width;
    private Double height;

    RectangleBuilder(PDFDancer client, int pageNumber) {
        this.path = new PathBuilder(client, pageNumber);
    }

    public RectangleBuilder at(double x, double y) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        this.x = x;
        this.y = y;
        return this;
    }

    public RectangleBuilder size(double width, double height) {
        if (!Double.isFinite(width) || !Double.isFinite(height) || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Rectangle width and height must be finite positive numbers");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    public RectangleBuilder color(Color color) { path.color(color); return this; }
    public RectangleBuilder fillColor(Color color) { path.fillColor(color); return this; }
    public RectangleBuilder lineWidth(double width) { path.lineWidth(width); return this; }
    public RectangleBuilder dash(double... pattern) { path.dash(pattern); return this; }
    public RectangleBuilder dashWithPhase(double phase, double... pattern) {
        path.dashWithPhase(phase, pattern);
        return this;
    }
    public RectangleBuilder solid() { path.solid(); return this; }
    public RectangleBuilder evenOddFill(boolean enabled) { path.evenOddFill(enabled); return this; }

    public boolean add() {
        if (x == null || y == null || width == null || height == null) {
            throw new IllegalStateException("Rectangle position and size are required");
        }
        return path.rect(x, y, width, height).add();
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
    }
}
