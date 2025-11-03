package com.tfc.pdf.pdfdancer.api.common.model;
/**
 * Represents an interactive form field within a PDF document.
 * This class encapsulates form field properties including type, value, appearance,
 * and behavior for various interactive PDF form elements such as text fields,
 * checkboxes, radio buttons, and dropdowns.
 */
public class Form extends PDFObject {
    /**
     * Enumeration of supported PDF form field types.
     */
    public enum FormType {
        /**
         * Single-line or multi-line text input field
         */
        TEXT_FIELD,
        /**
         * Checkbox for binary selections
         */
        CHECKBOX,
        /**
         * Radio button for mutually exclusive selections
         */
        RADIO_BUTTON,
        /**
         * Dropdown list with selectable options
         */
        DROPDOWN,
        /**
         * Clickable button element
         */
        BUTTON
    }
    /**
     * Field name identifier used in form processing.
     */
    private String name;
    /**
     * Type of form field determining its behavior and appearance.
     */
    private FormType type;
    /**
     * Current value or content of the form field.
     */
    private String value;
    /**
     * Dimensions of the form field's visual representation.
     */
    private Size size;
    /**
     * Font properties for text-based form fields.
     */
    private Font font;
    /**
     * Default constructor for serialization frameworks.
     */
    public Form() {
        super();
    }
    /**
     * Creates a form field with specified properties.
     *
     * @param id       unique identifier for the form field
     * @param name     field name for form processing
     * @param type     the type of form field to create
     * @param position location within the PDF document
     * @param size     dimensions of the form field
     */
    public Form(String id, String name, FormType type, Position position, Size size) {
        super(id, position);
        this.name = name;
        this.type = type;
        this.size = size;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public FormType getType() {
        return type;
    }
    public void setType(FormType type) {
        this.type = type;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Size getSize() {
        return size;
    }
    public void setSize(Size size) {
        this.size = size;
    }
    public Font getFont() {
        return font;
    }
    public void setFont(Font font) {
        this.font = font;
    }
    /**
     * Returns the object type for this form field.
     *
     * @return ObjectType.FORM indicating this is a form field object
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.FORM_X_OBJECT;
    }
}