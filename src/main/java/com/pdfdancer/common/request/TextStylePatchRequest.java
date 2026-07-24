package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStylePatchRequest {
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

    @JsonCreator
    public TextStylePatchRequest(@JsonProperty("font") String font,
                                 @JsonProperty("size") Double size,
                                 @JsonProperty("fillColor") PdfColorRequest fillColor,
                                 @JsonProperty("strokeColor") PdfColorRequest strokeColor,
                                 @JsonProperty("characterSpacing") Double characterSpacing,
                                 @JsonProperty("wordSpacing") Double wordSpacing) {
        this.font = font;
        this.size = size;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.characterSpacing = characterSpacing;
        this.wordSpacing = wordSpacing;
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

    public TextStylePatchRequest validated() {
        validateFields(font, size, fillColor, strokeColor, characterSpacing, wordSpacing);
        return this;
    }

    static void validateFields(String font,
                               Double size,
                               PdfColorRequest fillColor,
                               PdfColorRequest strokeColor,
                               Double characterSpacing,
                               Double wordSpacing) {
        if (font == null && size == null && fillColor == null && strokeColor == null &&
                characterSpacing == null && wordSpacing == null) {
            throw new IllegalArgumentException("style patch must set at least one field");
        }
        validateOptionalFields(font, size, fillColor, strokeColor, characterSpacing, wordSpacing);
    }

    static void validateOptionalFields(String font,
                                       Double size,
                                       PdfColorRequest fillColor,
                                       PdfColorRequest strokeColor,
                                       Double characterSpacing,
                                       Double wordSpacing) {
        if (font != null && font.isBlank()) {
            throw new IllegalArgumentException("font must not be blank");
        }
        if (size != null && (!Double.isFinite(size) || size <= 0.0)) {
            throw new IllegalArgumentException("size must be positive and finite");
        }
        if (fillColor != null) {
            fillColor.validated();
        }
        if (strokeColor != null) {
            strokeColor.validated();
        }
        if (characterSpacing != null && !Double.isFinite(characterSpacing)) {
            throw new IllegalArgumentException("characterSpacing must be finite");
        }
        if (wordSpacing != null && !Double.isFinite(wordSpacing)) {
            throw new IllegalArgumentException("wordSpacing must be finite");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStylePatchRequest) obj;
        return Objects.equals(font, that.font) &&
                Objects.equals(size, that.size) &&
                Objects.equals(fillColor, that.fillColor) &&
                Objects.equals(strokeColor, that.strokeColor) &&
                Objects.equals(characterSpacing, that.characterSpacing) &&
                Objects.equals(wordSpacing, that.wordSpacing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(font, size, fillColor, strokeColor, characterSpacing, wordSpacing);
    }

    @Override
    public String toString() {
        return "TextStylePatchRequest[" +
                "font=" + font + ", " +
                "size=" + size + ", " +
                "fillColor=" + fillColor + ", " +
                "strokeColor=" + strokeColor + ", " +
                "characterSpacing=" + characterSpacing + ", " +
                "wordSpacing=" + wordSpacing + ']';
    }

    public static final class Builder {
        private String font;
        private Double size;
        private PdfColorRequest fillColor;
        private PdfColorRequest strokeColor;
        private Double characterSpacing;
        private Double wordSpacing;

        private Builder() {
        }

        private Builder(TextStylePatchRequest patch) {
            if (patch != null) {
                this.font = patch.font();
                this.size = patch.size();
                this.fillColor = patch.fillColor();
                this.strokeColor = patch.strokeColor();
                this.characterSpacing = patch.characterSpacing();
                this.wordSpacing = patch.wordSpacing();
            }
        }

        static Builder from(TextStylePatchRequest patch) {
            return new Builder(patch);
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

        public TextStylePatchRequest build() {
            return new TextStylePatchRequest(font, size, fillColor, strokeColor, characterSpacing, wordSpacing).validated();
        }
    }
}
