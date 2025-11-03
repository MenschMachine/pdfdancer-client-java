package com.pdfdancer.common.model;

import com.fasterxml.jackson.annotation.*;

/**
 * Lightweight reference to a PDF object providing identity and type information.
 * Object references enable efficient API operations by providing a way to identify
 * and reference PDF objects without transferring their complete content.
 * This design pattern reduces payload sizes and improves performance for
 * operations that only need object identification and basic properties.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "objectRefType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextTypeObjectRef.class, name = "PARAGRAPH"),
        @JsonSubTypes.Type(value = TextTypeObjectRef.class, name = "TEXT_LINE"),
        @JsonSubTypes.Type(value = TextTypeObjectRef.class, name = "TEXT_ELEMENT"),
        @JsonSubTypes.Type(value = TextTypeObjectRef.class, name = "textElement"),
        @JsonSubTypes.Type(value = PageRef.class, name = "PAGE"),
        @JsonSubTypes.Type(value = FormFieldRef.class, name = "FORM_FIELD"),
        @JsonSubTypes.Type(value = FormFieldRef.class, name = "TEXT_FIELD"),
        @JsonSubTypes.Type(value = FormFieldRef.class, name = "CHECKBOX"),
        @JsonSubTypes.Type(value = FormFieldRef.class, name = "RADIO_BUTTON"),
        @JsonSubTypes.Type(value = ObjectRef.class, name = "PATH"),
        @JsonSubTypes.Type(value = ObjectRef.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ObjectRef.class, name = "FORM_X_OBJECT")
})
public class ObjectRef {
    /**
     * Internal identifier used to locate the object within the PDF processing system.
     * This ID is typically generated during PDF parsing and analysis and provides
     * a stable reference to the object across API operations.
     */
    private final String internalId;
    /**
     * Type classification of the referenced PDF object.
     * This enables type-safe operations and filtering without needing
     * to load the complete object data.
     */
    private final ObjectType type;
    private final ObjectType objectRefType;
    /**
     * Current getPosition and spatial information for the referenced object.
     * This may be updated as the object is moved or modified within the document.
     */
    private Position position;

    protected ObjectRef(String internalId, Position position, ObjectType objectRefType, ObjectType type) {
        this.internalId = internalId;
        this.position = position;
        ObjectType base = objectRefType != null ? objectRefType : type;
        this.objectRefType = base;
        this.type = type != null ? type : base;
    }

    protected ObjectRef(String internalId, Position position, ObjectType objectRefType) {
        this(internalId, position, objectRefType, objectRefType);
    }

    /**
     * Creates a new object reference with the specified properties.
     * This constructor initializes all the essential information needed
     * to identify and work with a PDF object through the API.
     *
     * @param internalId unique internal identifier for the object
     * @param position   current spatial getPosition of the object
     * @param type       classification of the object type
     */
    @JsonCreator
    public static ObjectRef create(@JsonProperty("internalId") String internalId,
                                   @JsonProperty("position") Position position,
                                   @JsonProperty("objectRefType") @JsonAlias("type") ObjectType objectRefType,
                                   @JsonProperty("type") @JsonAlias("objectRefType") ObjectType type) {
        return new ObjectRef(internalId, position, objectRefType, type);
    }

    /**
     * Returns the internal identifier for the referenced object.
     *
     * @return the internal ID string used by the PDF processing system
     */
    public String getInternalId() {
        return internalId;
    }

    /**
     * Returns the current getPosition information for the referenced object.
     *
     * @return getPosition object containing spatial location and bounds
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Updates the getPosition information for the referenced object.
     * This method allows updating the object's spatial information
     * as it may change through move operations.
     *
     * @param position new getPosition information for the object
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns the type classification of the referenced object.
     *
     * @return the ObjectType enum value indicating the object's category
     */
    @JsonProperty("type")
    public ObjectType getType() {
        return type;
    }

    @JsonProperty("objectRefType")
    public ObjectType getObjectRefType() {
        return objectRefType;
    }
}
