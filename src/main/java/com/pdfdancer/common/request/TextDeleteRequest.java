package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextDeleteRequest {
    @JsonProperty("pages")
    private final List<Integer> pages;
    @JsonProperty("select")
    private final TextSelectorRequest select;
    @JsonProperty("layout")
    private final TextLayoutRequest layout;

    @JsonCreator
    public TextDeleteRequest(@JsonProperty("pages") List<Integer> pages,
                             @JsonProperty("select") TextSelectorRequest select,
                             @JsonProperty("layout") TextLayoutRequest layout) {
        this.pages = pages == null ? null : List.copyOf(pages);
        this.select = select;
        this.layout = layout;
    }

    public static Builder literal(String text) {
        return new Builder().literal(text);
    }

    public static Builder regex(String regex) {
        return new Builder().regex(regex);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Integer> pages() { return pages; }
    public TextSelectorRequest select() { return select; }
    public TextLayoutRequest layout() { return layout; }

    public TextDeleteRequest withPages(List<Integer> pages) {
        return new TextDeleteRequest(pages, select, layout);
    }

    public TextDeleteRequest validated() {
        TextReplaceRequest.validatePages(pages);
        TextReplaceRequest.validateSelector(select);
        TextReplaceRequest.validateLayout(layout);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextDeleteRequest) obj;
        return Objects.equals(pages, that.pages) &&
                Objects.equals(select, that.select) &&
                Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages, select, layout);
    }

    @Override
    public String toString() {
        return "TextDeleteRequest[" +
                "pages=" + pages + ", " +
                "select=" + select + ", " +
                "layout=" + layout + ']';
    }

    public static final class Builder {
        private List<Integer> pages;
        private String literal;
        private String regex;
        private Boolean caseSensitive;
        private Boolean wholeWords;
        private Integer maxMatches;
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

        public TextDeleteRequest build() {
            TextSelectorRequest selector = new TextSelectorRequest(literal, regex, caseSensitive, wholeWords, maxMatches);
            return new TextDeleteRequest(pages, selector, layout).validated();
        }
    }
}
