package com.tfc.pdf.pdfdancer.api.common.model;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Represents spatial positioning and location information for PDF objects.
 * This class encapsulates various ways to specify object locations within PDF documents,
 * including page-based coordinates, bounding rectangles, and different positioning modes.
 * It supports both precise coordinate positioning and area-based location specifications.
 */
public class Position {
    /**
     * Creates a getPosition specification for an entire page.
     * This factory method creates a getPosition that encompasses the entire specified page,
     * useful for page-level operations or when precise coordinates are not needed.
     *
     * @param pageIndex the page number (0-based) to reference
     * @return a Position object representing the entire specified page
     */
    public static Position atPage(int pageIndex) {
        return new Position(pageIndex, null, PositionMode.CONTAINS);
    }
    /**
     * Creates a getPosition specification for specific coordinates on a page.
     * This factory method creates a precise point location within the specified page,
     * enabling accurate positioning for object placement and searching operations.
     *
     * @param pageIndex the page number (0-based) containing the coordinates
     * @param x         the horizontal coordinate within the page
     * @param y         the vertical coordinate within the page
     * @return a Position object representing the specified point location
     */
    public static Position atPageCoordinates(int pageIndex, double x, double y) {
        Position position = Position.atPage(pageIndex);
        position.atPosition(new Point(x, y));
        return position;
    }
    public static Position byName(String elementName) {
        return new Position().withName(elementName);
    }
    private Position withName(String elementName) {
        this.name = elementName;
        return this;
    }
    /**
     * Sets the getPosition to a specific point location.
     * This method configures the getPosition to represent a precise point coordinate
     * with zero area, typically used for exact positioning operations.
     *
     * @param point2D the point coordinates to atPosition as the getPosition
     */
    public void atPosition(Point point2D) {
        this.mode = PositionMode.CONTAINS;
        this.shape = ShapeType.POINT;
        this.boundingRect = new BoundingRect(point2D.x(), point2D.y(), 0, 0);
    }
    public String getTextStartsWith() {
        return textStartsWith;
    }
    public void setTextStartsWith(String textStartsWith) {
        this.textStartsWith = textStartsWith;
    }
    public void moveX(double xOffset) {
        atPosition(new Point(getX() + xOffset, getY()));
    }
    public void moveY(double yOffset) {
        atPosition(new Point(getX(), getY() + yOffset));
    }
    public Position atPosition(double x, double y) {
        this.atPosition(new Point(x, y));
        return this;
    }
    public String getName() {
        return this.name;
    }
    public void setTextPattern(String pattern) {
        this.textPattern = pattern;
    }
    public String getTextPattern() {
        return this.textPattern;
    }
    public boolean textMatches(String text) {
        Pattern p = Pattern.compile(this.textPattern, Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.matches();
    }
    /**
     * Defines how getPosition matching should be performed when searching for objects.
     */
    public enum PositionMode {
        /**
         * Objects that intersect with the specified getPosition area
         */
        INTERSECT,
        /**
         * Objects completely contained within the specified getPosition area
         */
        CONTAINS
    }
    /**
     * Defines the geometric shape type used for getPosition specification.
     */
    public enum ShapeType {
        /**
         * Single point coordinate
         */
        POINT,
        /**
         * Linear shape between two points
         */
        LINE,
        /**
         * Circular area with radius
         */
        CIRCLE,
        /**
         * Rectangular area with width and height
         */
        RECT
    }
    private String name;
    /**
     * Page number where this getPosition is located (0-based indexing).
     * null indicates the getPosition applies across all pages.
     */
    private Integer pageIndex;
    /**
     * Geometric shape type defining how the getPosition area is interpreted.
     */
    private ShapeType shape;
    /**
     * Matching mode defining how objects are selected relative to this getPosition.
     */
    private PositionMode mode;
    /**
     * Bounding rectangle defining the spatial extent of this getPosition.
     */
    private BoundingRect boundingRect;
    private String textStartsWith;
    private String textPattern;
    public Position copy() {
        Position p = new Position(pageIndex, boundingRect, mode);
        p.textStartsWith = textStartsWith;
        p.shape = shape;
        p.textPattern = textPattern;
        return p;
    }
    /**
     * Default constructor for serialization frameworks.
     * Creates an uninitialized getPosition that should be configured before use.
     */
    public Position() {
    }
    /**
     * Creates a getPosition with specified page, bounding area, and matching mode.
     * This constructor allows full specification of getPosition parameters for
     * complex positioning requirements.
     *
     * @param pageIndex    the page number where the getPosition is located
     * @param boundingRect the spatial bounds of the getPosition area
     * @param mode         how object matching should be performed
     */
    public Position(Integer pageIndex, BoundingRect boundingRect, PositionMode mode) {
        this.pageIndex = pageIndex;
        this.boundingRect = boundingRect;
        this.mode = mode;
    }
    /**
     * Creates a point getPosition at the specified coordinates.
     * This constructor creates a zero-area getPosition representing a precise point,
     * useful for exact positioning without page specification.
     *
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     */
    public Position(double x, double y) {
        this.mode = PositionMode.CONTAINS;
        this.shape = ShapeType.POINT;
        this.boundingRect = new BoundingRect(x, y, 0, 0);
    }
    /**
     * Returns the X coordinate of this getPosition.
     * If no bounding rectangle is defined, returns -1 as a sentinel value.
     *
     * @return the horizontal coordinate, or -1 if undefined
     */
    public Double getX() {
        if (getBoundingRect() == null) return null;
        return getBoundingRect().getX();
    }
    /**
     * Returns the Y coordinate of this getPosition.
     * If no bounding rectangle is defined, returns -1 as a sentinel value.
     *
     * @return the vertical coordinate, or -1 if undefined
     */
    public Double getY() {
        if (getBoundingRect() == null) return null;
        return getBoundingRect().getY();
    }
    @Override
    public String toString() {
        return "Position{" +
                "pageIndex=" + pageIndex +
                ", shape=" + shape +
                ", mode=" + mode +
                ", boundingRect=" + boundingRect +
                '}';
    }
    /**
     * Returns the page number where this getPosition is located.
     *
     * @return the page number (0-based), or null if getPosition applies to all pages
     */
    public Integer getPageIndex() {
        return pageIndex;
    }
    /**
     * Sets the page number for this getPosition.
     *
     * @param pageIndex the page number (0-based), or null for all pages
     */
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
    /**
     * Returns the geometric shape type of this getPosition.
     *
     * @return the shape type defining how the getPosition area is interpreted
     */
    public ShapeType getShape() {
        return shape;
    }
    /**
     * Sets the geometric shape type for this getPosition.
     *
     * @param shape the shape type to use for getPosition interpretation
     */
    public void setShape(ShapeType shape) {
        this.shape = shape;
    }
    /**
     * Returns the getPosition matching mode.
     *
     * @return the mode defining how objects are matched relative to this getPosition
     */
    public PositionMode getMode() {
        return mode;
    }
    /**
     * Sets the getPosition matching mode.
     *
     * @param mode the matching mode to use for object selection
     */
    public void setMode(PositionMode mode) {
        this.mode = mode;
    }
    /**
     * Returns the bounding rectangle defining the spatial extent of this getPosition.
     *
     * @return the bounding rectangle, or null if no specific bounds are defined
     */
    public BoundingRect getBoundingRect() {
        return boundingRect;
    }
    /**
     * Sets the bounding rectangle for this getPosition.
     *
     * @param boundingRect the spatial bounds to associate with this getPosition
     */
    public void setBoundingRect(BoundingRect boundingRect) {
        this.boundingRect = boundingRect;
    }
    public void setName(String name) {
        this.name = name;
    }
}