package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.Color;
import com.tfc.pdf.pdfdancer.api.common.model.Point;
import com.tfc.pdf.pdfdancer.api.common.model.Position;
import com.tfc.pdf.pdfdancer.api.common.model.path.Bezier;
import com.tfc.pdf.pdfdancer.api.common.model.path.Path;

/**
 * Fluent builder for adding a cubic Bezier curve to a PDF page.
 * <p>
 * Coordinate system: origin bottom-left; units in points (1/72").
 * The curve is added as a one-segment Path with STROKE painting by default.
 * <p>
 * Styling: color(Color), lineWidth(double), dash(...), dashWithPhase(...).
 */
public class BezierBuilder {

    private final PDFDancer client;
    private final int pageIndex;

    private Point p0; // start
    private Point c1; // control 1
    private Point c2; // control 2
    private Point p3; // end

    private Color strokeColor;
    private Color fillColor;
    private Double strokeWidth;
    private double[] dashArray;
    private Double dashPhase;
    private Boolean evenOddFill; // null -> default (nonzero)

    BezierBuilder(PDFDancer client, int pageIndex) {
        this.client = client;
        this.pageIndex = pageIndex;
    }

    public BezierBuilder from(double x, double y) {
        this.p0 = new Point(x, y);
        return this;
    }

    public BezierBuilder control1(double x, double y) {
        this.c1 = new Point(x, y);
        return this;
    }

    public BezierBuilder control2(double x, double y) {
        this.c2 = new Point(x, y);
        return this;
    }

    public BezierBuilder to(double x, double y) {
        this.p3 = new Point(x, y);
        return this;
    }

    public BezierBuilder color(Color color) {
        this.strokeColor = color;
        return this;
    }

    public BezierBuilder lineWidth(double width) {
        this.strokeWidth = width;
        return this;
    }

    public BezierBuilder fillColor(Color color) {
        this.fillColor = color;
        return this;
    }

    public BezierBuilder dash(double... pattern) {
        this.dashArray = pattern;
        this.dashPhase = 0.0;
        return this;
    }

    public BezierBuilder dashWithPhase(double phase, double... pattern) {
        this.dashArray = pattern;
        this.dashPhase = phase;
        return this;
    }

    public BezierBuilder evenOddFill(boolean evenOdd) {
        this.evenOddFill = evenOdd;
        return this;
    }

    public boolean add() {
        if (p0 == null) throw new IllegalArgumentException("Bezier start point (from) is not set");
        if (c1 == null) throw new IllegalArgumentException("Bezier control1 is not set");
        if (c2 == null) throw new IllegalArgumentException("Bezier control2 is not set");
        if (p3 == null) throw new IllegalArgumentException("Bezier end point (to) is not set");

        Bezier curve = new Bezier(p0, c1, c2, p3);
        if (strokeColor != null) curve.setStrokeColor(strokeColor);
        if (fillColor != null) curve.setFillColor(fillColor);
        if (strokeWidth != null) curve.setStrokeWidth(strokeWidth);
        if (dashArray != null && dashArray.length > 0) curve.setDashArray(dashArray);
        if (dashPhase != null) curve.setDashPhase(dashPhase);
        curve.setPosition(Position.atPageCoordinates(pageIndex, p0.x(), p0.y()));

        Path path = new Path();
        if (evenOddFill != null) {
            path.setEvenOddFill(evenOddFill);
        }
        path.addPathSegment(curve);
        return client.addObject(path);
    }
}

