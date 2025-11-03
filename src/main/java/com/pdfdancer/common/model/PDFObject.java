package com.pdfdancer.common.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pdfdancer.common.model.path.Line;
import com.pdfdancer.common.model.path.Path;
import com.pdfdancer.common.model.text.Paragraph;
import com.pdfdancer.common.model.text.TextLine;
import com.pdfdancer.common.model.path.Bezier;

/**
 * Abstract base class for all PDF objects that can be manipulated within the API.
 * This class provides the fundamental properties and behaviors shared by all PDF content elements,
 * including getPosition tracking, identification, and object reference generation.
 * <p>
 * The class uses Jackson polymorphic serialization to handle different concrete types
 * during JSON serialization/deserialization, enabling type-safe API operations
 * while maintaining flexibility for different PDF object types.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        // Accept both lowercase and uppercase discriminator names for robustness
        @JsonSubTypes.Type(value = Image.class, name = "image"),
        @JsonSubTypes.Type(value = Image.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = Path.class, name = "path"),
        @JsonSubTypes.Type(value = Path.class, name = "PATH"),
        @JsonSubTypes.Type(value = Form.class, name = "form"),
        @JsonSubTypes.Type(value = Form.class, name = "FORM"),
        @JsonSubTypes.Type(value = Paragraph.class, name = "paragraph"),
        @JsonSubTypes.Type(value = Paragraph.class, name = "PARAGRAPH"),
        @JsonSubTypes.Type(value = TextLine.class, name = "textLine"),
        @JsonSubTypes.Type(value = TextLine.class, name = "TEXT_LINE"),
        @JsonSubTypes.Type(value = Line.class, name = "line"),
        @JsonSubTypes.Type(value = Line.class, name = "LINE"),
        @JsonSubTypes.Type(value = Bezier.class, name = "bezier"),
        @JsonSubTypes.Type(value = Bezier.class, name = "BEZIER")
})
public abstract class PDFObject {
    /**
     * Unique identifier for this PDF object within the document context.
     * This ID is used to reference the object in API operations and
     * maintain consistency across different operations on the same object.
     */
    private String id;
    /**
     * Spatial getPosition and location information for this object within the PDF.
     * This includes page number, coordinates, and bounding area information
     * that defines where the object is located within the document.
     */
    private Position position;

    /**
     * Default constructor required for serialization frameworks.
     * Creates an uninitialized PDF object that should be populated
     * with appropriate values before use.
     */
    public PDFObject() {
    }

    /**
     * Constructs a PDF object with the specified identifier and getPosition.
     * This constructor initializes the fundamental properties that all
     * PDF objects require for proper identification and spatial location.
     *
     * @param id       unique identifier for the object within the document
     * @param position spatial location and positioning information
     */
    public PDFObject(String id, Position position) {
        this.id = id;
        this.position = position;
    }

    /**
     * Returns the unique identifier for this PDF object.
     *
     * @return the object's unique ID string
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this PDF object.
     *
     * @param id the unique ID string to assign to this object
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the spatial getPosition information for this object.
     *
     * @return the getPosition object containing location and bounds information
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Updates the spatial getPosition information for this object.
     *
     * @param position the new getPosition object with updated location information
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Creates an object reference for this PDF object.
     * Object references provide a lightweight way to refer to PDF objects
     * without including their full content, enabling efficient API operations.
     *
     * @return an ObjectRef containing the ID, getPosition, and type of this object
     */
    public ObjectRef toObjectRef() {
        return new ObjectRef(this.getId(), getPosition(), this.getObjectType());
    }

    /**
     * Returns the specific object type for this PDF object.
     * This method must be implemented by concrete subclasses to identify
     * their specific type, enabling type-safe operations and filtering.
     *
     * @return the ObjectType enum value representing this object's type
     */
    protected abstract ObjectType getObjectType();
}
