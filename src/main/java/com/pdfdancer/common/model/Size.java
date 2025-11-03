package com.pdfdancer.common.model;

/**
 * Represents dimensional measurements with width and height properties.
 * This class encapsulates size information for PDF objects (images, forms, etc.),
 * providing both individual dimension access and calculated area methods.
 */
public class Size {
    /**
     * Width dimension in PDF coordinate units.
     */
    private double width;
    /**
     * Height dimension in PDF coordinate units.
     */
    private double height;

    /**
     * Default constructor for serialization frameworks.
     */
    public Size() {
    }

    /**
     * Creates a size with specified width and height dimensions.
     *
     * @param width  the width dimension
     * @param height the height dimension
     */
    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width dimension.
     *
     * @return the width value
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width dimension.
     *
     * @param width the new width value
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Returns the height dimension.
     *
     * @return the height value
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height dimension.
     *
     * @param height the new height value
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Calculates and returns the total area.
     * This method computes the area by multiplying width and height,
     * useful for size comparisons and layout calculations.
     *
     * @return the calculated area (width × height)
     */
    public double getArea() {
        return width * height;
    }
}
