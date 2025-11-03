package com.tfc.pdf.pdfdancer.api.common.model.path;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tfc.pdf.pdfdancer.api.common.model.Color;
import com.tfc.pdf.pdfdancer.api.common.model.PDFObject;
import com.tfc.pdf.pdfdancer.api.common.model.Position;

/**
 * Abstract base class for individual path segments within vector paths.
 * This class provides common properties for path elements including stroke and fill
 * colors, line width, and positioning. Concrete subclasses implement specific
 * geometric shapes like lines, curves, and bezier segments.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "segmentType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Line.class, name = "line"),
        @JsonSubTypes.Type(value = Line.class, name = "LINE"),
        @JsonSubTypes.Type(value = Bezier.class, name = "bezier"),
        @JsonSubTypes.Type(value = Bezier.class, name = "BEZIER")
})
public abstract class PathSegment extends PDFObject {
    /**
     * Color used for drawing the segment's outline or stroke.
     */
    private Color strokeColor;
    /**
     * Color used for filling the segment's interior area (if applicable).
     */
    private Color fillColor;
    /**
     * Width of the stroke line in PDF coordinate units.
     */
    private double strokeWidth;
    /**
     * Dash pattern for stroking the path segment. Interpreted as alternating on/off lengths.
     * Null or empty means solid line. Values are in user space units.
     */
    private double[] dashArray;
    /**
     * Dash phase (offset) into the dash pattern in user space units. Null or zero means no offset.
     */
    private Double dashPhase;

    /**
     * Default constructor for serialization frameworks.
     */
    public PathSegment() {
        super();
    }

    /**
     * Creates a path segment with specified styling and getPosition properties.
     *
     * @param id          unique identifier for the path segment
     * @param strokeColor color for the segment outline
     * @param fillColor   color for the segment fill (null for no fill)
     * @param strokeWidth width of the stroke line
     * @param position    location within the PDF document
     */
    public PathSegment(String id, Color strokeColor, Color fillColor, double strokeWidth, Position position) {
        super(id, position);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.strokeWidth = strokeWidth;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public double[] getDashArray() {
        return dashArray;
    }

    public void setDashArray(double[] dashArray) {
        this.dashArray = dashArray;
    }

    public Double getDashPhase() {
        return dashPhase;
    }

    public void setDashPhase(Double dashPhase) {
        this.dashPhase = dashPhase;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}