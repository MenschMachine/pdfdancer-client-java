package com.tfc.pdf.pdfdancer.api.common.model;

/**
 * Represents font properties for text rendering in PDF documents.
 * This class encapsulates font characteristics including typeface name, size,
 * and styling attributes such as bold, italic, and underline formatting.
 * Used throughout the API for consistent text formatting and style management.
 */
public class Font {
    /**
     * Font family name (e.g., "Arial", "Times New Roman", "Helvetica").
     */
    private String name;
    /**
     * Font size in points.
     */
    private double size;
    private boolean isEmbedded;

    /**
     * Default constructor creating an uninitialized font.
     */
    public Font() {
    }

    /**
     * Creates a font with specified name and size, using regular styling.
     *
     * @param name font family name
     * @param size font size in points
     */
    public Font(String name, double size, boolean isEmbedded) {
        this.name = name;
        this.size = size;
        this.isEmbedded = isEmbedded;
    }

    public Font(String name, double size) {
        this(name, size, name.matches("^[A-Z]{6}\\+.*"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean embedded) {
        isEmbedded = embedded;
    }
}