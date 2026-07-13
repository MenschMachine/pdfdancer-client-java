package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStyleRequest {
    @JsonProperty("pages")
    private final List<Integer> pages;
    @JsonProperty("select")
    private final TextStyleSelectorRequest select;
    @JsonProperty("style")
    private final Style style;
    @JsonProperty("layout")
    private final TextLayoutRequest layout;

    @JsonCreator
    public TextStyleRequest(@JsonProperty("pages") List<Integer> pages,
                            @JsonProperty("select") TextStyleSelectorRequest select,
                            @JsonProperty("style") Style style,
                            @JsonProperty("layout") TextLayoutRequest layout) {
        this.pages = pages == null ? null : List.copyOf(pages);
        this.select = select;
        this.style = style;
        this.layout = layout;
    }

    public TextStyleRequest(List<Integer> pages,
                            TextSelectorRequest select,
                            Style style,
                            TextLayoutRequest layout) {
        this(pages, TextStyleSelectorRequest.from(select), style, layout);
    }

    public static Builder literal(String text) {
        return new Builder().literal(text);
    }

    public static Builder regex(String regex) {
        return new Builder().regex(regex);
    }

    public static Builder runsWhere() {
        return new Builder().runsWhere();
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Integer> pages() { return pages; }
    public TextStyleSelectorRequest select() { return select; }
    public Style style() { return style; }
    public TextLayoutRequest layout() { return layout; }

    public TextStyleRequest withPages(List<Integer> pages) {
        return new TextStyleRequest(pages, select, style, layout);
    }

    public TextStyleRequest validated() {
        TextReplaceRequest.validatePages(pages);
        if (select == null) {
            throw new IllegalArgumentException("select must not be null");
        }
        select.validated();
        validateStyle(style);
        TextReplaceRequest.validateLayout(layout);
        return this;
    }

    static void validateStyle(Style style) {
        if (style == null) {
            throw new IllegalArgumentException("style must not be null");
        }
        if (!style.hasAnyField()) {
            throw new IllegalArgumentException("style must set at least one field");
        }
        TextStylePatchRequest.validateOptionalFields(
                style.font(),
                style.size(),
                style.fillColor(),
                style.strokeColor(),
                style.characterSpacing(),
                style.wordSpacing());
        if (Boolean.TRUE.equals(style.resetSpacingOverrides()) &&
                (style.characterSpacing() != null || style.wordSpacing() != null)) {
            throw new IllegalArgumentException("resetSpacingOverrides cannot be combined with characterSpacing or wordSpacing");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStyleRequest) obj;
        return Objects.equals(pages, that.pages) &&
                Objects.equals(select, that.select) &&
                Objects.equals(style, that.style) &&
                Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages, select, style, layout);
    }

    @Override
    public String toString() {
        return "TextStyleRequest[" +
                "pages=" + pages + ", " +
                "select=" + select + ", " +
                "style=" + style + ", " +
                "layout=" + layout + ']';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Style {
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
        public Style(@JsonProperty("font") String font,
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

        public String font() { return font; }
        public Double size() { return size; }
        public PdfColorRequest fillColor() { return fillColor; }
        public PdfColorRequest strokeColor() { return strokeColor; }
        public Double characterSpacing() { return characterSpacing; }
        public Double wordSpacing() { return wordSpacing; }
        public Boolean resetSpacingOverrides() { return resetSpacingOverrides; }

        private boolean hasAnyField() {
            return font != null || size != null || fillColor != null || strokeColor != null ||
                    characterSpacing != null || wordSpacing != null || resetSpacingOverrides != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Style) obj;
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
            return Objects.hash(font, size, fillColor, strokeColor, characterSpacing, wordSpacing, resetSpacingOverrides);
        }

        @Override
        public String toString() {
            return "Style[" +
                    "font=" + font + ", " +
                    "size=" + size + ", " +
                    "fillColor=" + fillColor + ", " +
                    "strokeColor=" + strokeColor + ", " +
                    "characterSpacing=" + characterSpacing + ", " +
                    "wordSpacing=" + wordSpacing + ", " +
                    "resetSpacingOverrides=" + resetSpacingOverrides + ']';
        }
    }

    public static final class Builder {
        private List<Integer> pages;
        private String literal;
        private String regex;
        private Boolean caseSensitive;
        private Boolean wholeWords;
        private Integer maxMatches;
        private boolean runsWhere;
        private String whereTextContains;
        private String whereFont;
        private TextStyleNumericFilterRequest whereSize;
        private PdfColorRequest whereFillColor;
        private PdfColorRequest whereStrokeColor;
        private TextStyleNumericFilterRequest whereCharacterSpacing;
        private TextStyleNumericFilterRequest whereWordSpacing;
        private Boolean whereContainsUnmappedGlyphs;
        private String font;
        private Double size;
        private PdfColorRequest fillColor;
        private PdfColorRequest strokeColor;
        private Double characterSpacing;
        private Double wordSpacing;
        private Boolean resetSpacingOverrides;
        private TextLayoutRequest layout;

        private Builder() {
        }

        public Builder pages(Integer... pages) {
            this.pages = pages == null ? null : List.of(pages);
            return this;
        }

        public Builder pages(List<Integer> pages) {
            this.pages = pages == null ? null : List.copyOf(pages);
            return this;
        }

        public Builder literal(String literal) {
            this.literal = literal;
            this.regex = null;
            this.runsWhere = false;
            return this;
        }

        public Builder regex(String regex) {
            this.regex = regex;
            this.literal = null;
            this.runsWhere = false;
            return this;
        }

        public Builder runsWhere() {
            this.runsWhere = true;
            this.literal = null;
            this.regex = null;
            return this;
        }

        public Builder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public Builder wholeWords(boolean wholeWords) {
            this.wholeWords = wholeWords;
            return this;
        }

        public Builder maxMatches(int maxMatches) {
            this.maxMatches = maxMatches;
            return this;
        }

        public Builder whereTextContains(String textContains) {
            runsWhere();
            this.whereTextContains = textContains;
            return this;
        }

        public Builder whereFont(String font) {
            runsWhere();
            this.whereFont = font;
            return this;
        }

        public Builder whereSize(double eq) {
            runsWhere();
            this.whereSize = TextStyleNumericFilterRequest.eq(eq);
            return this;
        }

        public Builder whereSize(double eq, double tolerance) {
            runsWhere();
            this.whereSize = TextStyleNumericFilterRequest.eq(eq, tolerance);
            return this;
        }

        public Builder whereFillColor(PdfColorRequest fillColor) {
            runsWhere();
            this.whereFillColor = fillColor;
            return this;
        }

        public Builder whereStrokeColor(PdfColorRequest strokeColor) {
            runsWhere();
            this.whereStrokeColor = strokeColor;
            return this;
        }

        public Builder whereCharacterSpacing(double eq) {
            runsWhere();
            this.whereCharacterSpacing = TextStyleNumericFilterRequest.eq(eq);
            return this;
        }

        public Builder whereCharacterSpacing(double eq, double tolerance) {
            runsWhere();
            this.whereCharacterSpacing = TextStyleNumericFilterRequest.eq(eq, tolerance);
            return this;
        }

        public Builder whereWordSpacing(double eq) {
            runsWhere();
            this.whereWordSpacing = TextStyleNumericFilterRequest.eq(eq);
            return this;
        }

        public Builder whereWordSpacing(double eq, double tolerance) {
            runsWhere();
            this.whereWordSpacing = TextStyleNumericFilterRequest.eq(eq, tolerance);
            return this;
        }

        public Builder whereContainsUnmappedGlyphs(boolean containsUnmappedGlyphs) {
            runsWhere();
            this.whereContainsUnmappedGlyphs = containsUnmappedGlyphs;
            return this;
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

        public Builder resetSpacingOverrides(boolean resetSpacingOverrides) {
            this.resetSpacingOverrides = resetSpacingOverrides;
            return this;
        }

        public Builder sourceAnchored() {
            this.layout = TextLayoutRequest.sourceAnchored();
            return this;
        }

        public Builder reflowWhenSupported(TextLayoutRequest.Profile profile) {
            this.layout = TextLayoutRequest.reflowWhenSupported(profile);
            return this;
        }

        public Builder requireReflow(TextLayoutRequest.Profile profile) {
            this.layout = TextLayoutRequest.requireReflow(profile);
            return this;
        }

        public Builder layout(TextLayoutRequest layout) {
            this.layout = layout;
            return this;
        }

        public TextStyleRequest build() {
            TextStyleSelectorRequest selector = runsWhere
                    ? new TextStyleSelectorRequest(null, null, null, null, null,
                    new TextStyleRunsSelectorRequest(
                            new TextStyleRunFilterRequest(
                                    whereTextContains,
                                    whereFont,
                                    whereSize,
                                    whereFillColor,
                                    whereStrokeColor,
                                    whereCharacterSpacing,
                                    whereWordSpacing,
                                    whereContainsUnmappedGlyphs),
                            maxMatches))
                    : new TextStyleSelectorRequest(literal, regex, caseSensitive, wholeWords, maxMatches, null);
            Style style = new Style(font, size, fillColor, strokeColor, characterSpacing, wordSpacing, resetSpacingOverrides);
            return new TextStyleRequest(pages, selector, style, layout).validated();
        }
    }
}
