package com.tfc.pdf.pdfdancer.api.common.model.path;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectType;
import com.tfc.pdf.pdfdancer.api.common.model.PDFObject;
import com.tfc.pdf.pdfdancer.api.common.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complex vector path consisting of multiple path segments.
 * This class encapsulates vector graphics data within PDF documents,
 * composed of various path elements like lines, curves, and shapes.
 * Provides automatic getPosition calculation based on constituent segments.
 */
public class Path extends PDFObject {
    /**
     * Collection of path segments that compose this complete path.
     * Each segment represents a portion of the overall vector path.
     */
    private List<PathSegment> pathSegments = new ArrayList<>();
    /**
     * Optional fill rule. If true, uses even-odd fill rule; if false, uses nonzero.
     * If null, defaults to nonzero.
     */
    private Boolean evenOddFill;

    /**
     * Calculates the getPosition of this path based on its constituent segments.
     * This method determines the path's getPosition by finding the leftmost X coordinate
     * and topmost Y coordinate from all path segments, representing the path's origin.
     *
     * @return calculated getPosition representing the path's origin point
     */
    @Override
    public Position getPosition() {
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (PathSegment pathSegment : pathSegments) {
            Position position = pathSegment.getPosition();
            if (position.getX() < minX) {
                minX = position.getX();
            }
            if (position.getY() > maxY) {
                maxY = position.getY();
            }
        }
        return new Position(minX, maxY);
    }

    /**
     * Position setting is not supported for complex paths.
     * Path getPosition is automatically calculated from constituent segments.
     *
     * @param position ignored parameter
     * @throws UnsupportedOperationException always, as path getPosition is calculated
     */
    @Override
    @JsonIgnore
    public void setPosition(Position position) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the list of path segments that compose this path.
     *
     * @return list of path segments in the order they were added
     */
    public List<PathSegment> getPathSegments() {
        return pathSegments;
    }

    /**
     * Sets the path segments for this path.
     * Used for deserialization.
     *
     * @param pathSegments the list of path segments
     */
    public void setPathSegments(List<PathSegment> pathSegments) {
        this.pathSegments = pathSegments != null ? pathSegments : new ArrayList<>();
    }

    /**
     * Adds a new path segment to this path.
     * Segments are added in sequence and contribute to the overall path shape.
     *
     * @param pathSegment the path segment to add to this path
     */
    public void addPathSegment(PathSegment pathSegment) {
        this.pathSegments.add(pathSegment);
    }

    /**
     * Returns whether even-odd fill rule should be used (true) or nonzero (false). Null means default (nonzero).
     */
    public Boolean getEvenOddFill() {
        return evenOddFill;
    }

    /**
     * Sets the fill rule for this path. If true, uses even-odd; if false, nonzero; if null, defaults to nonzero.
     */
    public void setEvenOddFill(Boolean evenOddFill) {
        this.evenOddFill = evenOddFill;
    }

    /**
     * Returns the object type for this path.
     *
     * @return ObjectType.PATH indicating this is a vector path object
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.PATH;
    }
}
