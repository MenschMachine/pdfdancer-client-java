package com.tfc.pdf.pdfdancer.api.common.model;
import java.util.Objects;
/**
 * Represents color information using RGBA (Red, Green, Blue, Alpha) color model.
 * This class encapsulates color data for PDF objects, supporting both opaque and
 * transparent colors with integer values ranging from 0-255 for each component.
 * Provides convenient constructors and utility methods for color manipulation.
 */
public class Color {
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color RED = new Color(255, 0, 0);
    /**
     * Red color component (0-255).
     */
    private int red;
    /**
     * Green color component (0-255).
     */
    private int green;
    /**
     * Blue color component (0-255).
     */
    private int blue;
    /**
     * Alpha (transparency) component (0-255, where 255 is fully opaque).
     */
    private int alpha;
    /**
     * Default constructor creating a transparent black color.
     */
    public Color() {
    }
    /**
     * Creates an opaque color with specified RGB values.
     * Alpha is automatically atPosition to 255 (fully opaque).
     *
     * @param red   red component (0-255)
     * @param green green component (0-255)
     * @param blue  blue component (0-255)
     */
    public Color(int red, int green, int blue) {
        this(red, green, blue, 255);
    }
    /**
     * Creates a color with specified RGBA values.
     *
     * @param red   red component (0-255)
     * @param green green component (0-255)
     * @param blue  blue component (0-255)
     * @param alpha alpha component (0-255, where 255 is fully opaque)
     */
    public Color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
    public int getRed() {
        return red;
    }
    public void setRed(int red) {
        this.red = red;
    }
    public int getGreen() {
        return green;
    }
    public void setGreen(int green) {
        this.green = green;
    }
    public int getBlue() {
        return blue;
    }
    public void setBlue(int blue) {
        this.blue = blue;
    }
    public int getAlpha() {
        return alpha;
    }
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Color color)) return false;
        return red == color.red && green == color.green && blue == color.blue && alpha == color.alpha;
    }
    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }
    @Override
    public String toString() {
        return "Color{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", alpha=" + alpha +
                '}';
    }
    /**
     * Converts the RGB components to a hexadecimal color string.
     * This method generates a standard hex color representation (e.g., #FF0000 for red)
     * that can be used in web contexts or other color representations.
     * Note: Alpha component is not included in the hex representation.
     *
     * @return hex color string in format #RRGGBB
     */
    public String toHex() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}