package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStyleRunFilterRequest {
    @JsonProperty("textContains")
    private final String textContains;
    @JsonProperty("font")
    private final String font;
    @JsonProperty("size")
    private final TextStyleNumericFilterRequest size;
    @JsonProperty("fillColor")
    private final PdfColorRequest fillColor;
    @JsonProperty("strokeColor")
    private final PdfColorRequest strokeColor;
    @JsonProperty("characterSpacing")
    private final TextStyleNumericFilterRequest characterSpacing;
    @JsonProperty("wordSpacing")
    private final TextStyleNumericFilterRequest wordSpacing;
    @JsonProperty("containsUnmappedGlyphs")
    private final Boolean containsUnmappedGlyphs;

    @JsonCreator
    public TextStyleRunFilterRequest(@JsonProperty("textContains") String textContains,
                                     @JsonProperty("font") String font,
                                     @JsonProperty("size") TextStyleNumericFilterRequest size,
                                     @JsonProperty("fillColor") PdfColorRequest fillColor,
                                     @JsonProperty("strokeColor") PdfColorRequest strokeColor,
                                     @JsonProperty("characterSpacing") TextStyleNumericFilterRequest characterSpacing,
                                     @JsonProperty("wordSpacing") TextStyleNumericFilterRequest wordSpacing,
                                     @JsonProperty("containsUnmappedGlyphs") Boolean containsUnmappedGlyphs) {
        this.textContains = textContains;
        this.font = font;
        this.size = size;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.characterSpacing = characterSpacing;
        this.wordSpacing = wordSpacing;
        this.containsUnmappedGlyphs = containsUnmappedGlyphs;
    }

    public String textContains() { return textContains; }
    public String font() { return font; }
    public TextStyleNumericFilterRequest size() { return size; }
    public PdfColorRequest fillColor() { return fillColor; }
    public PdfColorRequest strokeColor() { return strokeColor; }
    public TextStyleNumericFilterRequest characterSpacing() { return characterSpacing; }
    public TextStyleNumericFilterRequest wordSpacing() { return wordSpacing; }
    public Boolean containsUnmappedGlyphs() { return containsUnmappedGlyphs; }

    public TextStyleRunFilterRequest validated() {
        if (!hasAnyField()) {
            throw new IllegalArgumentException("runs.where must set at least one filter");
        }
        if (textContains != null && textContains.isBlank()) {
            throw new IllegalArgumentException("runs.where.textContains must not be blank");
        }
        if (font != null && font.isBlank()) {
            throw new IllegalArgumentException("runs.where.font must not be blank");
        }
        if (size != null) {
            size.validated();
        }
        if (fillColor != null) {
            fillColor.validated();
        }
        if (strokeColor != null) {
            strokeColor.validated();
        }
        if (characterSpacing != null) {
            characterSpacing.validated();
        }
        if (wordSpacing != null) {
            wordSpacing.validated();
        }
        return this;
    }

    private boolean hasAnyField() {
        return textContains != null || font != null || size != null || fillColor != null ||
                strokeColor != null || characterSpacing != null || wordSpacing != null ||
                containsUnmappedGlyphs != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStyleRunFilterRequest) obj;
        return Objects.equals(textContains, that.textContains) &&
                Objects.equals(font, that.font) &&
                Objects.equals(size, that.size) &&
                Objects.equals(fillColor, that.fillColor) &&
                Objects.equals(strokeColor, that.strokeColor) &&
                Objects.equals(characterSpacing, that.characterSpacing) &&
                Objects.equals(wordSpacing, that.wordSpacing) &&
                Objects.equals(containsUnmappedGlyphs, that.containsUnmappedGlyphs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textContains, font, size, fillColor, strokeColor,
                characterSpacing, wordSpacing, containsUnmappedGlyphs);
    }

    @Override
    public String toString() {
        return "TextStyleRunFilterRequest[" +
                "textContains=" + textContains + ", " +
                "font=" + font + ", " +
                "size=" + size + ", " +
                "fillColor=" + fillColor + ", " +
                "strokeColor=" + strokeColor + ", " +
                "characterSpacing=" + characterSpacing + ", " +
                "wordSpacing=" + wordSpacing + ", " +
                "containsUnmappedGlyphs=" + containsUnmappedGlyphs + ']';
    }
}
