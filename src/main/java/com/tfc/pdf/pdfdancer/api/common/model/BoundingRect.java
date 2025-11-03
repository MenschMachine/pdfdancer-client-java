package com.tfc.pdf.pdfdancer.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a rectangular area defined by getPosition and dimensions.
 * This class encapsulates bounding box information for PDF objects,
 * providing spatial extent data used for positioning, collision detection,
 * and layout operations within PDF documents.
 */
public class BoundingRect {
    /**
     * X-coordinate of the rectangle's origin (left edge).
     */
    private double x;
    /**
     * Y-coordinate of the rectangle's origin (bottom edge in PDF coordinates).
     */
    private double y;
    /**
     * Width of the rectangle extending rightward from the origin.
     */
    private double width;
    /**
     * Height of the rectangle extending upward from the origin.
     */
    private double height;

    /**
     * Creates a bounding rectangle with specified getPosition and dimensions.
     *
     * @param x      the X-coordinate of the rectangle's origin
     * @param y      the Y-coordinate of the rectangle's origin
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     */
    @JsonCreator
    public BoundingRect(@JsonProperty("x") double x,
                        @JsonProperty("y") double y,
                        @JsonProperty("width") double width,
                        @JsonProperty("height") double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the origin point of this bounding rectangle.
     * The origin represents the bottom-left corner in PDF coordinate system.
     *
     * @return a Point object representing the rectangle's origin coordinates
     */
    public Point getOrigin() {
        return new Point(x, y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "BoundingRect{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoundingRect)) return false;
        BoundingRect that = (BoundingRect) o;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(width, that.width) == 0 && Double.compare(height, that.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
