package com.tfc.pdf.pdfdancer.api.common.model.path;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectType;
import com.tfc.pdf.pdfdancer.api.common.model.Point;
/**
 * Represents a straight line path segment between two points.
 * This class defines a linear path element connecting two coordinate points,
 * commonly used in vector graphics and geometric shapes within PDF documents.
 */
public class Line extends PathSegment {
    /**
     * Starting point of the line segment.
     */
    private Point p0;
    /**
     * Ending point of the line segment.
     */
    private Point p1;

    /**
     * Default constructor for deserialization.
     */
    public Line() {
        super();
    }

    /**
     * Creates a line segment between the specified points.
     *
     * @param p0 starting point of the line
     * @param p1 ending point of the line
     */
    @JsonCreator
    public Line(@JsonProperty("p0") Point p0,
                @JsonProperty("p1") Point p1) {
        this.p0 = p0;
        this.p1 = p1;
    }
    /**
     * Returns the ending point of this line segment.
     * 
     * @return the line's end point
     */
    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }
    /**
     * Returns the starting point of this line segment.
     * 
     * @return the line's start point
     */
    public Point getP0() {
        return p0;
    }

    public void setP0(Point p0) {
        this.p0 = p0;
    }
    /**
     * Returns the object type for this line segment.
     * 
     * @return ObjectType.LINE indicating this is a line segment
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.LINE;
    }
}
