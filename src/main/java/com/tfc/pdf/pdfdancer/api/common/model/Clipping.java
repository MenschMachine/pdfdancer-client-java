package com.tfc.pdf.pdfdancer.api.common.model;

import com.tfc.pdf.pdfdancer.api.common.model.path.PathSegment;

/**
 * Represents a clipping region that masks or limits the visibility of other path segments.
 * This class defines clipping boundaries within PDF documents, controlling which parts
 * of underlying content are visible. Clipping regions are defined by path segments
 * that establish the visible area boundaries.
 */
public class Clipping extends PathSegment {
    /**
     * The path segment that defines the clipping boundary.
     * Only content within this path's bounds will be visible.
     */
    private PathSegment clippedPath;

    /**
     * Default constructor for serialization frameworks.
     */
    public Clipping() {
        super();
    }

    /**
     * Creates a clipping region with specified boundary path.
     *
     * @param id          unique identifier for the clipping region
     * @param position    location within the PDF document
     * @param clippedPath path segment defining the clipping boundary
     */
    public Clipping(String id, Position position, PathSegment clippedPath) {
        super(id, null, null, 0.0, position);
        this.clippedPath = clippedPath;
    }

    /**
     * Returns the path segment defining the clipping boundary.
     *
     * @return the clipping boundary path segment
     */
    public PathSegment getClippedPath() {
        return clippedPath;
    }

    /**
     * Sets the path segment that defines the clipping boundary.
     *
     * @param clippedPath the path segment to use as clipping boundary
     */
    public void setClippedPath(PathSegment clippedPath) {
        this.clippedPath = clippedPath;
    }

    /**
     * Returns the object type for this clipping region.
     *
     * @return ObjectType.CLIPPING indicating this is a clipping region
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.CLIPPING;
    }
}