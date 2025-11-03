package com.tfc.pdf.pdfdancer.api.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextTypeObjectRef extends ObjectRef {
    private final String fontName;
    private final Double fontSize;
    private final List<Double> lineSpacings;
    private final List<TextTypeObjectRef> children;
    private final Color color;
    private final String text;
    private final TextStatus status;
    /**
     * Creates a new object reference with the specified properties.
     * This constructor initializes all the essential information needed
     * to identify and work with a PDF object through the API.
     *
     * @param internalId   unique internal identifier for the object
     * @param position     current spatial getPosition of the object
     * @param type         classification of the object type
     * @param fontName     name of the font used by the object
     * @param fontSize     size of the font used by the object
     * @param text         text content of the object
     * @param lineSpacings list of line spacing factors (not absolute distances), e.g., 1.2 means 1.2 * fontSize
     * @param color        RGBA color applied to the text content (null if unspecified)
     * @param status       text status information
     */
    @JsonCreator
    public TextTypeObjectRef(@JsonProperty("internalId") String internalId,
                             @JsonProperty("position") Position position,
                             @JsonProperty("type") @JsonAlias("objectRefType") ObjectType type,
                             @JsonProperty("objectRefType") ObjectType objectRefType,
                             @JsonProperty("fontName") String fontName,
                             @JsonProperty("fontSize") Double fontSize,
                             @JsonProperty("text") String text,
                             @JsonProperty("lineSpacings") List<Double> lineSpacings,
                             @JsonProperty("color") Color color,
                             @JsonProperty("status") TextStatus status,
                             @JsonProperty("children") List<TextTypeObjectRef> children) {
        super(internalId, position, objectRefType, type);
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.text = text;
        this.lineSpacings = lineSpacings != null ? new ArrayList<>(lineSpacings) : new ArrayList<>();
        this.color = color;
        this.status = status;
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }
    public String getText() {
        if (this.text == null) {
            return this.children.stream().map(TextTypeObjectRef::getText).collect(Collectors.joining("\n"));
        } else {
            return text;
        }
    }
    public String getFontName() {
        return fontName;
    }
    public Double getFontSize() {
        return fontSize;
    }
    /**
     * Gets the line spacing factors between consecutive lines.
     *
     * @return list of spacing factors (not absolute distances), e.g., 1.2 means 1.2 * fontSize pixels between baselines
     */
    public List<Double> getLineSpacings() {
        return lineSpacings;
    }
    public List<TextTypeObjectRef> getChildren() {
        return children;
    }
    public Color getColor() {
        return color;
    }
    public void addChild(TextTypeObjectRef child) {
        this.children.add(child);
    }
    public TextStatus getStatus() {
        return status;
    }
}
