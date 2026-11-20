package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PdfAffineTransform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextReplaceRequest {
    @JsonProperty("pages")
    private final List<Integer> pages;
    @JsonProperty("select")
    private final TextSelectorRequest select;
    @JsonProperty("replaceWith")
    private final String replaceWith;
    @JsonProperty("replaceWithImage")
    private final TextReplacementImageRequest replaceWithImage;
    @JsonProperty("style")
    private final TextStyleSetRequest style;
    @JsonProperty("layout")
    private final TextLayoutRequest layout;

    /**
     * Creates a request.
     *
     * <p>An image replace needs a {@code sourceAnchored} layout. When {@code replaceWithImage} is
     * set and {@code layout} is {@code null}, the request gets {@link TextLayoutRequest#sourceAnchored()}.
     * {@link #layout()} and the JSON that the SDK sends then show this layout.
     */
    @JsonCreator
    public TextReplaceRequest(@JsonProperty("pages") List<Integer> pages,
                              @JsonProperty("select") TextSelectorRequest select,
                              @JsonProperty("replaceWith") String replaceWith,
                              @JsonProperty("replaceWithImage") TextReplacementImageRequest replaceWithImage,
                              @JsonProperty("style") TextStyleSetRequest style,
                              @JsonProperty("layout") TextLayoutRequest layout) {
        this.pages = copyPages(pages);
        this.select = select;
        this.replaceWith = replaceWith;
        this.replaceWithImage = replaceWithImage;
        this.style = style;
        this.layout = layout == null && replaceWithImage != null ? TextLayoutRequest.sourceAnchored() : layout;
    }

    public TextReplaceRequest(List<Integer> pages,
                              TextSelectorRequest select,
                              String replaceWith,
                              TextReplacementImageRequest replaceWithImage,
                              TextLayoutRequest layout) {
        this(pages, select, replaceWith, replaceWithImage, null, layout);
    }

    public TextReplaceRequest(List<Integer> pages,
                              TextSelectorRequest select,
                              String replaceWith,
                              TextLayoutRequest layout) {
        this(pages, select, replaceWith, null, null, layout);
    }

    public static Builder literal(String text, String replaceWith) {
        return new Builder().literal(text).replaceWith(replaceWith);
    }

    public static Builder regex(String regex, String replaceWith) {
        return new Builder().regex(regex).replaceWith(replaceWith);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Integer> pages() { return pages; }
    public TextSelectorRequest select() { return select; }
    public String replaceWith() { return replaceWith; }
    public TextReplacementImageRequest replaceWithImage() { return replaceWithImage; }
    public TextStyleSetRequest style() { return style; }
    public TextLayoutRequest layout() { return layout; }

    public TextReplaceRequest withPages(List<Integer> pages) {
        return new TextReplaceRequest(pages, select, replaceWith, replaceWithImage, style, layout);
    }

    /**
     * Validates the request and returns this request. It does not change the request.
     */
    public TextReplaceRequest validated() {
        validatePages(pages);
        validateSelector(select);
        if ((replaceWith == null) == (replaceWithImage == null)) {
            throw new IllegalArgumentException("Exactly one of replaceWith or replaceWithImage is required");
        }
        if (replaceWithImage != null) {
            if (style != null) {
                throw new IllegalArgumentException("style is not valid with replaceWithImage");
            }
            byte[] data = replaceWithImage.data();
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("replaceWithImage image data is required and must not be empty");
            }
            if (replaceWithImage.transformation() == null) {
                throw new IllegalArgumentException("replaceWithImage transformation is required");
            }
            if (layout != null && layout.alignment() != null) {
                throw new IllegalArgumentException("layout.alignment is not valid with replaceWithImage");
            }
            if (layout == null || layout.mode() != TextLayoutRequest.Mode.sourceAnchored) {
                throw new IllegalArgumentException("replaceWithImage supports only sourceAnchored layout");
            }
        }
        if (style != null) {
            style.validated();
        }
        validateLayout(layout);
        return this;
    }

    /**
     * Copies a page list. Unlike {@link List#copyOf(java.util.Collection)}, this keeps a null
     * element, so that {@link #validatePages(List)} can report it as an
     * {@link IllegalArgumentException}.
     */
    static List<Integer> copyPages(List<Integer> pages) {
        return pages == null ? null : Collections.unmodifiableList(new ArrayList<>(pages));
    }

    static void validatePages(List<Integer> pages) {
        if (pages == null) {
            return;
        }
        for (Integer page : pages) {
            if (page == null || page < 1) {
                throw new IllegalArgumentException("pages must contain only page numbers >= 1");
            }
        }
    }

    static void validateSelector(TextSelectorRequest select) {
        if (select == null) {
            throw new IllegalArgumentException("select must not be null");
        }
        boolean hasLiteral = select.literal() != null;
        boolean hasRegex = select.regex() != null;
        if (hasLiteral == hasRegex) {
            throw new IllegalArgumentException("Exactly one of literal or regex must be provided");
        }
        if (hasLiteral && select.literal().isBlank()) {
            throw new IllegalArgumentException("literal must not be blank");
        }
        if (hasRegex && select.regex().isBlank()) {
            throw new IllegalArgumentException("regex must not be blank");
        }
        if (select.maxMatches() != null && select.maxMatches() <= 0) {
            throw new IllegalArgumentException("maxMatches must be positive");
        }
    }

    static void validateLayout(TextLayoutRequest layout) {
        if (layout == null) {
            return;
        }
        // The API treats an omitted mode as reflowWhenSupported.
        TextLayoutRequest.Mode mode = layout.mode() == null
                ? TextLayoutRequest.Mode.reflowWhenSupported
                : layout.mode();
        if (mode == TextLayoutRequest.Mode.sourceAnchored && layout.profile() != null) {
            throw new IllegalArgumentException("sourceAnchored layout must not specify profile");
        }
        if (mode == TextLayoutRequest.Mode.sourceAnchored && layout.hyphenationEnabled() != null) {
            throw new IllegalArgumentException(
                    "layout.hyphenationEnabled is not valid when layout.mode is sourceAnchored");
        }
        if (mode == TextLayoutRequest.Mode.sourceAnchored && layout.alignment() != null) {
            throw new IllegalArgumentException(
                    "layout.alignment is not valid when layout.mode is sourceAnchored");
        }
        if ((mode == TextLayoutRequest.Mode.reflowWhenSupported
                || mode == TextLayoutRequest.Mode.requireReflow)
                && layout.profile() == null) {
            throw new IllegalArgumentException(
                    mode + " profile must be one of default, bodyText, noReflow");
        }
    }

    /** Rejects {@code layout.alignment} on operations other than text replace. */
    static void rejectAlignment(TextLayoutRequest layout) {
        if (layout != null && layout.alignment() != null) {
            throw new IllegalArgumentException("layout.alignment is valid only for text replace");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextReplaceRequest) obj;
        return Objects.equals(pages, that.pages) &&
                Objects.equals(select, that.select) &&
                Objects.equals(replaceWith, that.replaceWith) &&
                Objects.equals(replaceWithImage, that.replaceWithImage) &&
                Objects.equals(style, that.style) &&
                Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages, select, replaceWith, replaceWithImage, style, layout);
    }

    @Override
    public String toString() {
        return "TextReplaceRequest[" +
                "pages=" + pages + ", " +
                "select=" + select + ", " +
                "replaceWith=" + replaceWith + ", " +
                "replaceWithImage=" + replaceWithImage + ", " +
                "style=" + style + ", " +
                "layout=" + layout + ']';
    }

    public static final class Builder {
        private List<Integer> pages;
        private String literal;
        private String regex;
        private Boolean caseSensitive;
        private Boolean wholeWords;
        private Integer maxMatches;
        private String replaceWith;
        private TextReplacementImageRequest replaceWithImage;
        private TextStyleSetRequest style;
        private TextStyleSetRequest.Builder styleBuilder;
        private TextLayoutRequest layout;
        private Boolean hyphenationEnabled;
        /** The alignment that you set with alignment(...). */
        private TextLayoutRequest.Alignment alignment;
        /** The alignment of the layout object that you passed to layout(...). */
        private TextLayoutRequest.Alignment layoutAlignment;

        private Builder() {
        }

        public Builder pages(Integer... pages) {
            this.pages = pages == null ? null : copyPages(Arrays.asList(pages));
            return this;
        }

        public Builder pages(List<Integer> pages) {
            this.pages = copyPages(pages);
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

        public Builder replaceWith(String replaceWith) {
            this.replaceWith = replaceWith;
            this.replaceWithImage = null;
            return this;
        }

        public Builder replaceWithImage(Image image, PdfAffineTransform transformation) {
            Objects.requireNonNull(image, "image");
            this.replaceWith = null;
            this.replaceWithImage = new TextReplacementImageRequest(image.getData(), transformation);
            return this;
        }

        public Builder replaceWithImage(File imageFile, PdfAffineTransform transformation) throws IOException {
            Objects.requireNonNull(imageFile, "imageFile");
            return replaceWithImage(Image.fromFile(imageFile), transformation);
        }

        public Builder style(TextStyleSetRequest style) {
            this.style = style;
            this.styleBuilder = null;
            return this;
        }

        public Builder font(String font) {
            styleBuilder().font(font);
            return this;
        }

        public Builder size(double size) {
            styleBuilder().size(size);
            return this;
        }

        public Builder fillColor(PdfColorRequest fillColor) {
            styleBuilder().fillColor(fillColor);
            return this;
        }

        public Builder strokeColor(PdfColorRequest strokeColor) {
            styleBuilder().strokeColor(strokeColor);
            return this;
        }

        public Builder characterSpacing(double characterSpacing) {
            styleBuilder().characterSpacing(characterSpacing);
            return this;
        }

        public Builder wordSpacing(double wordSpacing) {
            styleBuilder().wordSpacing(wordSpacing);
            return this;
        }

        public Builder resetSpacingOverrides() {
            styleBuilder().resetSpacingOverrides();
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

        public Builder sourceAnchored() {
            this.layout = TextLayoutRequest.sourceAnchored();
            this.hyphenationEnabled = null;
            this.layoutAlignment = null;
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

        /**
         * Aligns each edited line explicitly. Valid only for text replace.
         *
         * <p>The request always sends an explicit {@code reflowWhenSupported} mode with this
         * alignment. Without a layout, it uses the {@code noReflow} profile, the API default for
         * an omitted layout. You can combine it with
         * {@link #reflowWhenSupported(TextLayoutRequest.Profile)} and
         * {@link #requireReflow(TextLayoutRequest.Profile)} in any order.
         * {@link #sourceAnchored()} and {@code replaceWithImage(...)} are rejected at
         * {@link #build()}, in any order.
         *
         * <p>Which alignment applies:
         * <ul>
         *   <li>An alignment that you set with this method stays until you call this method
         *       again. {@link #layout(TextLayoutRequest)} keeps it, unless the layout that it gets
         *       has its own alignment. In that case, the alignment of the layout replaces it.</li>
         *   <li>An alignment inside a layout object belongs to that layout. The next call of
         *       {@link #layout(TextLayoutRequest)} or {@link #sourceAnchored()} replaces the
         *       layout, and its alignment goes with it.
         *       {@link #reflowWhenSupported(TextLayoutRequest.Profile)} and
         *       {@link #requireReflow(TextLayoutRequest.Profile)} change only the mode and the
         *       profile, so they keep the alignment.</li>
         *   <li>A call of this method after {@link #layout(TextLayoutRequest)} replaces the
         *       alignment of that layout.</li>
         * </ul>
         *
         * <p>The API reports an alignment that it cannot apply as an error with code
         * {@code TextOperationDiagnostic.ALIGNMENT_NOT_HONORED}. The API then does not change the
         * text of the unit named in the error. Other matches can still change. The response is
         * returned without an exception. Check {@code response.errors()} together with
         * {@code response.changed()}.
         */
        public Builder alignment(TextLayoutRequest.Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder layout(TextLayoutRequest layout) {
            this.layout = layout;
            this.hyphenationEnabled = layout == null ? null : layout.hyphenationEnabled();
            this.layoutAlignment = layout == null ? null : layout.alignment();
            if (layoutAlignment != null) {
                // The alignment of this layout replaces an alignment set with alignment(...).
                this.alignment = null;
            }
            return this;
        }

        public TextReplaceRequest build() {
            TextSelectorRequest selector = new TextSelectorRequest(literal, regex, caseSensitive, wholeWords, maxMatches);
            TextStyleSetRequest replacementStyle = styleBuilder == null ? style : styleBuilder.build();
            return new TextReplaceRequest(
                    pages, selector, replaceWith, replaceWithImage, replacementStyle, resolvedLayout()).validated();
        }

        private TextLayoutRequest resolvedLayout() {
            TextLayoutRequest resolved;
            if (layout == null) {
                resolved = hyphenationEnabled == null
                        ? null
                        : new TextLayoutRequest(null, null, hyphenationEnabled);
            } else {
                resolved = new TextLayoutRequest(layout.mode(), layout.profile(), hyphenationEnabled);
            }
            // alignment(...) after layout(...) replaces the alignment of that layout.
            TextLayoutRequest.Alignment effectiveAlignment = alignment != null ? alignment : layoutAlignment;
            if (effectiveAlignment == null) {
                return resolved;
            }
            if (resolved == null) {
                return TextLayoutRequest.reflowWhenSupported(TextLayoutRequest.Profile.NO_REFLOW)
                        .withAlignment(effectiveAlignment);
            }
            // An omitted mode means reflowWhenSupported in the API. Send it explicitly, so that
            // alignment(...) sends the same mode with and without hyphenationEnabled(...).
            TextLayoutRequest.Mode mode = resolved.mode() == null
                    ? TextLayoutRequest.Mode.reflowWhenSupported
                    : resolved.mode();
            return new TextLayoutRequest(
                    mode, resolved.profile(), resolved.hyphenationEnabled(), effectiveAlignment);
        }

        private TextStyleSetRequest.Builder styleBuilder() {
            if (styleBuilder == null) {
                styleBuilder = TextStyleSetRequest.Builder.from(style);
            }
            return styleBuilder;
        }
    }
}
