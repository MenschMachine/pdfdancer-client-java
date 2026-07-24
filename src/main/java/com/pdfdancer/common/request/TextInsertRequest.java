package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextInsertRequest {
    @JsonProperty("target")
    private final Target target;
    @JsonProperty("insert")
    private final String insert;
    @JsonProperty("style")
    private final Style style;
    @JsonProperty("layout")
    private final TextLayoutRequest layout;

    @JsonCreator
    public TextInsertRequest(@JsonProperty("target") Target target,
                             @JsonProperty("insert") String insert,
                             @JsonProperty("style") Style style,
                             @JsonProperty("layout") TextLayoutRequest layout) {
        this.target = target;
        this.insert = insert;
        this.style = style;
        this.layout = layout;
    }

    public static Builder after(String anchorLiteral, String insert) {
        return new Builder().literal(anchorLiteral).insert(insert).caret(Caret.after);
    }

    public static Builder before(String anchorLiteral, String insert) {
        return new Builder().literal(anchorLiteral).insert(insert).caret(Caret.before);
    }

    public static Builder afterRegex(String anchorRegex, String insert) {
        return new Builder().regex(anchorRegex).insert(insert).caret(Caret.after);
    }

    public static Builder beforeRegex(String anchorRegex, String insert) {
        return new Builder().regex(anchorRegex).insert(insert).caret(Caret.before);
    }

    public static Builder at(int page, double x, double y, String insert) {
        return new Builder().coordinate(page, x, y).insert(insert);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Target target() { return target; }
    public String insert() { return insert; }
    public Style style() { return style; }
    public TextLayoutRequest layout() { return layout; }

    public TextInsertRequest withPages(List<Integer> pages) {
        Target scopedTarget = target == null ? null : target.withPages(pages);
        return new TextInsertRequest(scopedTarget, insert, style, layout);
    }

    public TextInsertRequest validated() {
        return validated(false);
    }

    private TextInsertRequest validated(boolean allowMissingCoordinatePage) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        boolean hasAnchor = target.anchor() != null;
        boolean hasCoordinate = target.coordinate() != null;
        if (hasAnchor == hasCoordinate) {
            throw new IllegalArgumentException("Exactly one of target.anchor or target.coordinate must be provided");
        }
        if (hasAnchor) {
            validateAnchorTarget(target.anchor());
        } else {
            validateCoordinateTarget(target.coordinate(), allowMissingCoordinatePage);
        }
        if (insert == null || insert.isEmpty()) {
            throw new IllegalArgumentException("insert must not be null or empty");
        }
        validateStyleForTarget(hasAnchor);
        TextReplaceRequest.validateLayout(layout);
        return this;
    }

    private static void validateAnchorTarget(AnchorTarget anchor) {
        TextReplaceRequest.validatePages(anchor.pages());
        TextReplaceRequest.validateSelector(anchor.select());
        if (anchor.caret() == null) {
            throw new IllegalArgumentException("target.anchor.caret must not be null");
        }
    }

    private static void validateCoordinateTarget(CoordinateTarget coordinate, boolean allowMissingPage) {
        if (coordinate.page() == null) {
            if (!allowMissingPage) {
                throw new IllegalArgumentException("target.coordinate.page must be >= 1");
            }
        } else if (coordinate.page() < 1) {
            throw new IllegalArgumentException("target.coordinate.page must be >= 1");
        }
        if (coordinate.x() == null || !Double.isFinite(coordinate.x())) {
            throw new IllegalArgumentException("target.coordinate.x must be finite");
        }
        if (coordinate.y() == null || !Double.isFinite(coordinate.y())) {
            throw new IllegalArgumentException("target.coordinate.y must be finite");
        }
        if (coordinate.rotationDegrees() != null && !Double.isFinite(coordinate.rotationDegrees())) {
            throw new IllegalArgumentException("target.coordinate.rotationDegrees must be finite");
        }
    }

    private void validateStyleForTarget(boolean hasAnchor) {
        if (style == null) {
            throw new IllegalArgumentException("style must not be null");
        }
        if (hasAnchor && style.from() == null) {
            throw new IllegalArgumentException("style.from must be anchor");
        }
        style.validate(hasAnchor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextInsertRequest) obj;
        return Objects.equals(target, that.target) &&
                Objects.equals(insert, that.insert) &&
                Objects.equals(style, that.style) &&
                Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, insert, style, layout);
    }

    @Override
    public String toString() {
        return "TextInsertRequest[" +
                "target=" + target + ", " +
                "insert=" + insert + ", " +
                "style=" + style + ", " +
                "layout=" + layout + ']';
    }

    public enum Caret {
        before,
        after
    }

    public enum StyleFrom {
        anchor
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Target {
        @JsonProperty("anchor")
        private final AnchorTarget anchor;
        @JsonProperty("coordinate")
        private final CoordinateTarget coordinate;

        @JsonCreator
        public Target(@JsonProperty("anchor") AnchorTarget anchor,
                      @JsonProperty("coordinate") CoordinateTarget coordinate) {
            this.anchor = anchor;
            this.coordinate = coordinate;
        }

        public Target(AnchorTarget anchor) {
            this(anchor, null);
        }

        public AnchorTarget anchor() { return anchor; }
        public CoordinateTarget coordinate() { return coordinate; }

        private Target withPages(List<Integer> pages) {
            CoordinateTarget scopedCoordinate = coordinate;
            if (coordinate != null && pages != null && pages.size() == 1) {
                scopedCoordinate = coordinate.withPage(pages.get(0));
            }
            return new Target(anchor == null ? null : anchor.withPages(pages), scopedCoordinate);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Target) obj;
            return Objects.equals(anchor, that.anchor) &&
                    Objects.equals(coordinate, that.coordinate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(anchor, coordinate);
        }

        @Override
        public String toString() {
            return "Target[anchor=" + anchor + ", coordinate=" + coordinate + ']';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class AnchorTarget {
        @JsonProperty("pages")
        private final List<Integer> pages;
        @JsonProperty("select")
        private final TextSelectorRequest select;
        @JsonProperty("caret")
        private final Caret caret;

        @JsonCreator
        public AnchorTarget(@JsonProperty("pages") List<Integer> pages,
                            @JsonProperty("select") TextSelectorRequest select,
                            @JsonProperty("caret") Caret caret) {
            this.pages = pages == null ? null : List.copyOf(pages);
            this.select = select;
            this.caret = caret;
        }

        public List<Integer> pages() { return pages; }
        public TextSelectorRequest select() { return select; }
        public Caret caret() { return caret; }

        private AnchorTarget withPages(List<Integer> pages) {
            return new AnchorTarget(pages, select, caret);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (AnchorTarget) obj;
            return Objects.equals(pages, that.pages) &&
                    Objects.equals(select, that.select) &&
                    caret == that.caret;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pages, select, caret);
        }

        @Override
        public String toString() {
            return "AnchorTarget[" +
                    "pages=" + pages + ", " +
                    "select=" + select + ", " +
                    "caret=" + caret + ']';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class CoordinateTarget {
        @JsonProperty("page")
        private final Integer page;
        @JsonProperty("x")
        private final Double x;
        @JsonProperty("y")
        private final Double y;
        @JsonProperty("rotationDegrees")
        private final Double rotationDegrees;

        @JsonCreator
        public CoordinateTarget(@JsonProperty("page") Integer page,
                                @JsonProperty("x") Double x,
                                @JsonProperty("y") Double y,
                                @JsonProperty("rotationDegrees") Double rotationDegrees) {
            this.page = page;
            this.x = x;
            this.y = y;
            this.rotationDegrees = rotationDegrees;
        }

        public Integer page() { return page; }
        public Double x() { return x; }
        public Double y() { return y; }
        public Double rotationDegrees() { return rotationDegrees; }

        private CoordinateTarget withPage(Integer page) {
            return new CoordinateTarget(page, x, y, rotationDegrees);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (CoordinateTarget) obj;
            return Objects.equals(page, that.page) &&
                    Objects.equals(x, that.x) &&
                    Objects.equals(y, that.y) &&
                    Objects.equals(rotationDegrees, that.rotationDegrees);
        }

        @Override
        public int hashCode() {
            return Objects.hash(page, x, y, rotationDegrees);
        }

        @Override
        public String toString() {
            return "CoordinateTarget[" +
                    "page=" + page + ", " +
                    "x=" + x + ", " +
                    "y=" + y + ", " +
                    "rotationDegrees=" + rotationDegrees + ']';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Style {
        @JsonProperty("from")
        private final StyleFrom from;
        @JsonProperty("patch")
        private final TextStylePatchRequest patch;

        @JsonCreator
        public Style(@JsonProperty("from") StyleFrom from,
                     @JsonProperty("patch") TextStylePatchRequest patch) {
            this.from = from;
            this.patch = patch;
        }

        public static Style anchor() {
            return new Style(StyleFrom.anchor, null);
        }

        public static Style anchor(TextStylePatchRequest patch) {
            return new Style(StyleFrom.anchor, patch);
        }

        public StyleFrom from() { return from; }
        public TextStylePatchRequest patch() { return patch; }

        private void validate(boolean anchorTarget) {
            if (anchorTarget) {
                if (from != StyleFrom.anchor) {
                    throw new IllegalArgumentException("style.from must be anchor");
                }
                if (patch != null) {
                    patch.validated();
                }
            } else {
                if (from != null) {
                    throw new IllegalArgumentException("style.from must be omitted for coordinate insertion");
                }
                if (patch == null) {
                    throw new IllegalArgumentException("style.patch must not be null for coordinate insertion");
                }
                patch.validated();
                if (patch.font() == null || patch.font().isBlank()) {
                    throw new IllegalArgumentException("style.patch.font must not be blank for coordinate insertion");
                }
                if (patch.size() == null || !Double.isFinite(patch.size()) || patch.size() <= 0.0) {
                    throw new IllegalArgumentException("style.patch.size must be positive and finite for coordinate insertion");
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Style) obj;
            return from == that.from &&
                    Objects.equals(patch, that.patch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, patch);
        }

        @Override
        public String toString() {
            return "Style[from=" + from + ", patch=" + patch + ']';
        }
    }

    public static final class Builder {
        private List<Integer> pages;
        private String literal;
        private String regex;
        private Boolean caseSensitive;
        private Boolean wholeWords;
        private Integer maxMatches;
        private Caret caret;
        private Integer coordinatePage;
        private Double coordinateX;
        private Double coordinateY;
        private Double rotationDegrees;
        private boolean coordinateTarget;
        private String insert;
        private TextStylePatchRequest stylePatch;
        private TextStylePatchRequest.Builder stylePatchBuilder;
        private TextLayoutRequest layout;
        private Boolean hyphenationEnabled;

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
            return this;
        }

        public Builder regex(String regex) {
            this.regex = regex;
            this.literal = null;
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

        public Builder before() {
            return caret(Caret.before);
        }

        public Builder after() {
            return caret(Caret.after);
        }

        public Builder caret(Caret caret) {
            this.caret = caret;
            return this;
        }

        public Builder coordinate(int page, double x, double y) {
            this.coordinateTarget = true;
            this.coordinatePage = page;
            this.coordinateX = x;
            this.coordinateY = y;
            this.literal = null;
            this.regex = null;
            this.caret = null;
            return this;
        }

        public Builder coordinate(double x, double y) {
            this.coordinateTarget = true;
            this.coordinatePage = null;
            this.coordinateX = x;
            this.coordinateY = y;
            this.literal = null;
            this.regex = null;
            this.caret = null;
            return this;
        }

        public Builder rotationDegrees(double rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
            return this;
        }

        public Builder insert(String insert) {
            this.insert = insert;
            return this;
        }

        public Builder stylePatch(TextStylePatchRequest stylePatch) {
            this.stylePatch = stylePatch;
            this.stylePatchBuilder = null;
            return this;
        }

        public Builder font(String font) {
            stylePatchBuilder().font(font);
            return this;
        }

        public Builder size(double size) {
            stylePatchBuilder().size(size);
            return this;
        }

        public Builder fillColor(PdfColorRequest fillColor) {
            stylePatchBuilder().fillColor(fillColor);
            return this;
        }

        public Builder strokeColor(PdfColorRequest strokeColor) {
            stylePatchBuilder().strokeColor(strokeColor);
            return this;
        }

        public Builder characterSpacing(double characterSpacing) {
            stylePatchBuilder().characterSpacing(characterSpacing);
            return this;
        }

        public Builder wordSpacing(double wordSpacing) {
            stylePatchBuilder().wordSpacing(wordSpacing);
            return this;
        }

        public Builder sourceAnchored() {
            this.layout = TextLayoutRequest.sourceAnchored();
            this.hyphenationEnabled = null;
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

        public Builder hyphenationEnabled(boolean enabled) {
            this.hyphenationEnabled = enabled;
            return this;
        }

        public Builder layout(TextLayoutRequest layout) {
            this.layout = layout;
            this.hyphenationEnabled = layout == null ? null : layout.hyphenationEnabled();
            return this;
        }

        public TextInsertRequest build() {
            TextStylePatchRequest patch = stylePatchBuilder == null ? stylePatch : stylePatchBuilder.build();
            Target target;
            Style insertStyle;
            if (coordinateTarget) {
                target = new Target(null, new CoordinateTarget(coordinatePage, coordinateX, coordinateY, rotationDegrees));
                insertStyle = new Style(null, patch);
            } else {
                TextSelectorRequest selector = new TextSelectorRequest(literal, regex, caseSensitive, wholeWords, maxMatches);
                AnchorTarget anchor = new AnchorTarget(pages, selector, caret);
                target = new Target(anchor);
                insertStyle = Style.anchor(patch);
            }
            return new TextInsertRequest(target, insert, insertStyle, resolvedLayout())
                    .validated(coordinateTarget && coordinatePage == null);
        }

        private TextLayoutRequest resolvedLayout() {
            if (layout == null) {
                return hyphenationEnabled == null
                        ? null
                        : new TextLayoutRequest(null, null, hyphenationEnabled);
            }
            return new TextLayoutRequest(layout.mode(), layout.profile(), hyphenationEnabled);
        }

        private TextStylePatchRequest.Builder stylePatchBuilder() {
            if (stylePatchBuilder == null) {
                stylePatchBuilder = TextStylePatchRequest.Builder.from(stylePatch);
            }
            return stylePatchBuilder;
        }
    }
}
