package com.pdfdancer.common.model.text;

import com.pdfdancer.common.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a paragraph or text block within a PDF document.
 * This class encapsulates paragraph-level text content with unified formatting
 * properties and access to individual character elements for detailed manipulation.
 * Provides both block-level text operations and character-level granularity.
 */
public class TextLine extends PDFObject {
    private List<TextElement> textElements;
    private Color color;
    private Double fontSize;
    private String fontName;
    private String text;

    /**
     * Default constructor for serialization frameworks.
     */
    public TextLine() {
        super();
    }

    public static TextLine fromText(String line, Position position, Color color, Font font, TextStatus status) {
        TextLine textLine = new TextLine();
        textLine.setPosition(position);
        TextElement elem = new TextElement(null, line, font, color, position);
        elem.setStatus(status);
        textLine.setTextElements(List.of(elem));
        textLine.setColor(color);
        textLine.setFontName(font.getName());
        textLine.setFontSize(font.getSize());
        return textLine;
    }

    public static TextLine fromObjectRef(TextTypeObjectRef text) {
        TextLine textLine = new TextLine();
        textLine.setPosition(text.getPosition());
        Font font = new Font(text.getFontName(), text.getFontSize());
        TextElement elem = new TextElement(null, text.getText(), font, text.getColor(), text.getPosition());
        elem.setStatus(text.getStatus());
        textLine.setTextElements(List.of(elem));
        textLine.setColor(text.getColor());
        textLine.setFontName(text.getFontName());
        textLine.setFontSize(text.getFontSize());
        textLine.setText(text.getText());
        return textLine;
    }

    public Double getFontSize() {
        return fontSize;
    }

    public void setFontSize(Double fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
     * Returns the object type for this paragraph.
     *
     * @return ObjectType.PARAGRAPH indicating this is a paragraph object
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.TEXT_LINE;
    }

    public List<TextElement> getTextElements() {
        return textElements;
    }

    public void setTextElements(List<TextElement> textElements) {
        this.textElements = textElements;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public TextTypeObjectRef toObjectRef() {
        return new TextTypeObjectRef(this.getId(),
                getPosition(),
                this.getObjectType(),
                ObjectType.PARAGRAPH,
                this.getFontName(),
                this.getFontSize(),
                this.getText(),
                List.of(),
                this.getColor(),
                TextStatus.fromTextLine(this),
                this.getTextElements().stream().map(TextElement::toObjectRef).collect(Collectors.toUnmodifiableList())
        );
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
