package com.tfc.pdf.pdfdancer.api.common.model.text;
import com.tfc.pdf.pdfdancer.api.common.model.*;
import java.util.Collections;
/**
 * Represents a single character or glyph within a PDF document.
 * This class encapsulates individual character properties including the character
 * content, font styling, color, and getPosition for fine-grained text manipulation
 * and character-level formatting operations.
 */
public class TextElement extends PDFObject {
    /**
     * The character or glyph content (typically a single character string).
     */
    private String text;
    /**
     * Font properties for rendering this character.
     */
    private Font font;
    /**
     * Color properties for rendering this character.
     */
    private Color color;
    private TextStatus status;
    /**
     * Default constructor for serialization frameworks.
     */
    public TextElement() {
        super();
    }
    /**
     * Creates a character with specified properties.
     *
     * @param id       unique identifier for the character
     * @param text     the character content (typically single character)
     * @param font     font properties for rendering
     * @param color    color properties for rendering
     * @param position location within the PDF document
     */
    public TextElement(String id, String text, Font font, Color color, Position position) {
        super(id, position);
        this.text = text;
        this.font = font;
        this.color = color;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public Font getFont() {
        return font;
    }
    public void setFont(Font font) {
        this.font = font;
    }
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    /**
     * Returns the object type for this character.
     *
     * @return ObjectType.CHARACTER indicating this is a character object
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.TEXT_ELEMENT;
    }
    public void setStatus(TextStatus status) {
        this.status = status;
    }
    public TextStatus getStatus() {
        return status;
    }
    @Override
    public TextTypeObjectRef toObjectRef() {
        return new TextTypeObjectRef(
                this.getId(),
                this.getPosition(),
                this.getObjectType(),
                this.getObjectType(),
                this.getFont().getName(),
                this.getFont().getSize(),
                this.getText(),
                Collections.emptyList(),
                this.getColor(),
                this.getStatus(),
                Collections.emptyList()
        );
    }
}
