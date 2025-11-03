package com.tfc.pdf.pdfdancer.api.common.model.path;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectType;
import com.tfc.pdf.pdfdancer.api.common.model.Point;
/**
 * Represents a cubic Bezier curve path segment defined by four control points.
 * This class implements a cubic Bezier curve with start point, two control points,
 * and end point, providing smooth curved path segments for complex vector graphics.
 * Includes mathematical evaluation methods for point calculation along the curve.
 */
public class Bezier extends PathSegment {
    /**
     * Starting point of the Bezier curve.
     */
    private Point p0;
    /**
     * First control point affecting curve shape near the start.
     */
    private Point p1;
    /**
     * Second control point affecting curve shape near the end.
     */
    private Point p2;
    /**
     * Ending point of the Bezier curve.
     */
    private Point p3;

    /** Default constructor for deserialization. */
    public Bezier() {
        super();
    }

    /**
     * Creates a cubic Bezier curve with the specified control points.
     *
     * @param p0 starting point of the curve
     * @param p1 first control point influencing curve shape
     * @param p2 second control point influencing curve shape
     * @param p3 ending point of the curve
     */
    @JsonCreator
    public Bezier(@JsonProperty("p0") Point p0,
                  @JsonProperty("p1") Point p1,
                  @JsonProperty("p2") Point p2,
                  @JsonProperty("p3") Point p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }
    /**
     * Evaluates a point on the Bezier curve for the given parameter value.
     * This method uses the cubic Bezier mathematical formula to calculate
     * the exact coordinates at any point along the curve.
     * 
     * @param t parameter value between 0.0 and 1.0, where 0.0 is the start point
     *          and 1.0 is the end point
     * @return the calculated point on the curve at parameter t
     */
    @SuppressWarnings("unused")
    public Point evaluate(double t) {
        double x = Math.pow(1 - t, 3) * p0.x()
                + 3 * Math.pow(1 - t, 2) * t * p1.x()
                + 3 * (1 - t) * Math.pow(t, 2) * p2.x()
                + Math.pow(t, 3) * p3.x();
        double y = Math.pow(1 - t, 3) * p0.y()
                + 3 * Math.pow(1 - t, 2) * t * p1.y()
                + 3 * (1 - t) * Math.pow(t, 2) * p2.y()
                + Math.pow(t, 3) * p3.y();
        return new Point(x, y);
    }
    /**
     * Returns the object type for this Bezier curve segment.
     * 
     * @return ObjectType.BEZIER indicating this is a Bezier curve segment
     */
    /**
     * Returns the starting point p0 of this Bezier segment.
     */
    public Point getP0() { return p0; }

    public void setP0(Point p0) { this.p0 = p0; }
    /**
     * Returns the first control point p1 of this Bezier segment.
     */
    public Point getP1() { return p1; }

    public void setP1(Point p1) { this.p1 = p1; }
    /**
     * Returns the second control point p2 of this Bezier segment.
     */
    public Point getP2() { return p2; }

    public void setP2(Point p2) { this.p2 = p2; }
    /**
     * Returns the ending point p3 of this Bezier segment.
     */
    public Point getP3() { return p3; }

    public void setP3(Point p3) { this.p3 = p3; }
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.BEZIER;
    }
}
