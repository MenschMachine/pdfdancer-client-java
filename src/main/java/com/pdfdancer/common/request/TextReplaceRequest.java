package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PdfAffineTransform;

import java.io.File;
import java.io.IOException;
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
    @JsonProperty("layout")
    private final TextLayoutRequest layout;

    @JsonCreator
    public TextReplaceRequest(@JsonProperty("pages") List<Integer> pages,
                              @JsonProperty("select") TextSelectorRequest select,
                              @JsonProperty("replaceWith") String replaceWith,
                              @JsonProperty("replaceWithImage") TextReplacementImageRequest replaceWithImage,
                              @JsonProperty("layout") TextLayoutRequest layout) {
        this.pages = pages == null ? null : List.copyOf(pages);
        this.select = select;
        this.replaceWith = replaceWith;
        this.replaceWithImage = replaceWithImage;
        this.layout = layout;
    }

    public TextReplaceRequest(List<Integer> pages,
                              TextSelectorRequest select,
                              String replaceWith,
                              TextLayoutRequest layout) {
        this(pages, select, replaceWith, null, layout);
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
    public TextLayoutRequest layout() { return layout; }

    public TextReplaceRequest withPages(List<Integer> pages) {
        return new TextReplaceRequest(pages, select, replaceWith, replaceWithImage, layout);
    }

    public TextReplaceRequest validated() {
        validatePages(pages);
        validateSelector(select);
        if ((replaceWith == null) == (replaceWithImage == null)) {
            throw new IllegalArgumentException("Exactly one of replaceWith or replaceWithImage is required");
        }
        if (replaceWithImage != null) {
            byte[] data = replaceWithImage.data();
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("replaceWithImage image data is required and must not be empty");
            }
            if (replaceWithImage.transformation() == null) {
                throw new IllegalArgumentException("replaceWithImage transformation is required");
            }
            if (layout != null && layout.mode() != null
                    && layout.mode() != TextLayoutRequest.Mode.sourceAnchored) {
                throw new IllegalArgumentException("replaceWithImage supports only sourceAnchored layout");
            }
        }
        validateLayout(layout);
        return this;
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
        if (layout == null || layout.mode() == null) {
            return;
        }
        if (layout.mode() == TextLayoutRequest.Mode.sourceAnchored && layout.profile() != null) {
            throw new IllegalArgumentException("sourceAnchored layout must not specify profile");
        }
        if ((layout.mode() == TextLayoutRequest.Mode.reflowWhenSupported
                || layout.mode() == TextLayoutRequest.Mode.requireReflow)
                && layout.profile() == null) {
            throw new IllegalArgumentException(
                    layout.mode() + " profile must be one of default, bodyText, noReflow");
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
                Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages, select, replaceWith, replaceWithImage, layout);
    }

    @Override
    public String toString() {
        return "TextReplaceRequest[" +
                "pages=" + pages + ", " +
                "select=" + select + ", " +
                "replaceWith=" + replaceWith + ", " +
                "replaceWithImage=" + replaceWithImage + ", " +
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

        public TextReplaceRequest build() {
            TextSelectorRequest selector = new TextSelectorRequest(literal, regex, caseSensitive, wholeWords, maxMatches);
            return new TextReplaceRequest(pages, selector, replaceWith, replaceWithImage, layout).validated();
        }
    }
}
