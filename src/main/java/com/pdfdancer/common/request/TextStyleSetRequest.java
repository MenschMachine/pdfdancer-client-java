package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Style overrides applied atomically to replacement text.
 * Omitted fields preserve the corresponding source-text style.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStyleSetRequest {
    @JsonProperty("font")
    private final String font;
    @JsonProperty("size")
    private final Double size;
    @JsonProperty("fillColor")
    private final PdfColorRequest fillColor;
    @JsonProperty("strokeColor")
    private final PdfColorRequest strokeColor;
    @JsonProperty("characterSpacing")
    private final Double characterSpacing;
    @JsonProperty("wordSpacing")
    private final Double wordSpacing;
    @JsonProperty("resetSpacingOverrides")
    private final Boolean resetSpacingOverrides;

    @JsonCreator
    public TextStyleSetRequest(@JsonProperty("font") String font,
                               @JsonProperty("size") Double size,
                               @JsonProperty("fillColor") PdfColorRequest fillColor,
                               @JsonProperty("strokeColor") PdfColorRequest strokeColor,
                               @JsonProperty("characterSpacing") Double characterSpacing,
                               @JsonProperty("wordSpacing") Double wordSpacing,
                               @JsonProperty("resetSpacingOverrides") Boolean resetSpacingOverrides) {
        this.font = font;
        this.size = size;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.characterSpacing = characterSpacing;
        this.wordSpacing = wordSpacing;
        this.resetSpacingOverrides = resetSpacingOverrides;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String font() { return font; }
    public Double size() { return size; }
    public PdfColorRequest fillColor() { return fillColor; }
    public PdfColorRequest strokeColor() { return strokeColor; }
    public Double characterSpacing() { return characterSpacing; }
    public Double wordSpacing() { return wordSpacing; }
    public Boolean resetSpacingOverrides() { return resetSpacingOverrides; }

    public TextStyleSetRequest validated() {
        if (font == null && size == null && fillColor == null && strokeColor == null &&
                characterSpacing == null && wordSpacing == null && resetSpacingOverrides == null) {
            throw new IllegalArgumentException("style must contain at least one field");
        }
        TextStylePatchRequest.validateOptionalFields(
                font, size, fillColor, strokeColor, characterSpacing, wordSpacing);
        if (resetSpacingOverrides != null && !resetSpacingOverrides) {
            throw new IllegalArgumentException("resetSpacingOverrides must be true when present");
        }
        if (Boolean.TRUE.equals(resetSpacingOverrides) &&
                (characterSpacing != null || wordSpacing != null)) {
            throw new IllegalArgumentException(
                    "resetSpacingOverrides cannot be combined with characterSpacing or wordSpacing");
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStyleSetRequest) obj;
        return Objects.equals(font, that.font) &&
                Objects.equals(size, that.size) &&
                Objects.equals(fillColor, that.fillColor) &&
                Objects.equals(strokeColor, that.strokeColor) &&
                Objects.equals(characterSpacing, that.characterSpacing) &&
                Objects.equals(wordSpacing, that.wordSpacing) &&
                Objects.equals(resetSpacingOverrides, that.resetSpacingOverrides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(font, size, fillColor, strokeColor, characterSpacing,
                wordSpacing, resetSpacingOverrides);
    }

    @Override
    public String toString() {
        return "TextStyleSetRequest[" +
                "font=" + font + ", " +
                "size=" + size + ", " +
                "fillColor=" + fillColor + ", " +
                "strokeColor=" + strokeColor + ", " +
                "characterSpacing=" + characterSpacing + ", " +
                "wordSpacing=" + wordSpacing + ", " +
                "resetSpacingOverrides=" + resetSpacingOverrides + ']';
    }

    public static final class Builder {
        private String font;
        private Double size;
        private PdfColorRequest fillColor;
        private PdfColorRequest strokeColor;
        private Double characterSpacing;
        private Double wordSpacing;
        private Boolean resetSpacingOverrides;

        private Builder() {
        }

        private Builder(TextStyleSetRequest style) {
            if (style != null) {
                font = style.font();
                size = style.size();
                fillColor = style.fillColor();
                strokeColor = style.strokeColor();
                characterSpacing = style.characterSpacing();
                wordSpacing = style.wordSpacing();
                resetSpacingOverrides = style.resetSpacingOverrides();
            }
        }

        static Builder from(TextStyleSetRequest style) {
            return new Builder(style);
        }

        public Builder font(String font) {
            this.font = font;
            return this;
        }

        public Builder size(double size) {
            this.size = size;
            return this;
        }

        public Builder fillColor(PdfColorRequest fillColor) {
            this.fillColor = fillColor;
            return this;
        }

        public Builder strokeColor(PdfColorRequest strokeColor) {
            this.strokeColor = strokeColor;
            return this;
        }

        public Builder characterSpacing(double characterSpacing) {
            this.characterSpacing = characterSpacing;
            return this;
        }

        public Builder wordSpacing(double wordSpacing) {
            this.wordSpacing = wordSpacing;
            return this;
        }

        public Builder resetSpacingOverrides() {
            this.resetSpacingOverrides = true;
            return this;
        }

        public TextStyleSetRequest build() {
            return new TextStyleSetRequest(font, size, fillColor, strokeColor,
                    characterSpacing, wordSpacing, resetSpacingOverrides).validated();
        }
    }
}
