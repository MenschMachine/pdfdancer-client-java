package com.tfc.pdf.pdfdancer.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Immutable point record representing 2D coordinates.
 * This record provides a simple and efficient way to represent coordinate pairs
 * within PDF documents, with conversion utilities for interoperability with
 * Java's standard geometry classes.
 */
public final class Point {
    @JsonProperty("x")
    private final double x;
    @JsonProperty("y")
    private final double y;

    /**
     *
     */
    @JsonCreator
    public Point(@JsonProperty("x") double x,
                 @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a Point from a Java AWT Point2D instance.
     * This factory method enables conversion from standard Java geometry objects
     * to the API's coordinate representation.
     *
     * @param p the Point2D object to convert (must not be null)
     * @return a new Point with the same coordinates
     * @throws IllegalArgumentException if the input point is null
     */
    public static Point fromPoint2D(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException("p cannot be null");
        }
        return new Point(p.getX(), p.getY());
    }

    /**
     * Converts this Point to a Java AWT Point2D instance.
     * This method enables interoperability with standard Java geometry libraries
     * and graphics operations that require Point2D objects.
     *
     * @return a new Point2D.Double with the same coordinates
     */
    public Point2D toPoint2D() {
        return new Point2D.Double(x, y);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Point) obj;
        return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
                Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point[" +
                "x=" + x + ", " +
                "y=" + y + ']';
    }

}
