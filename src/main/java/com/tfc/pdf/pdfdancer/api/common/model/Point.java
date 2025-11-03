package com.tfc.pdf.pdfdancer.api.common.model;
import java.awt.geom.Point2D;
/**
 * Immutable point record representing 2D coordinates.
 * This record provides a simple and efficient way to represent coordinate pairs
 * within PDF documents, with conversion utilities for interoperability with
 * Java's standard geometry classes.
 */
public record Point(double x, double y) {
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
}
