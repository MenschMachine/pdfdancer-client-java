package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.ReflowPreset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request for batch template placeholder replacements in a PDF document.
 * Supports both document-level (all pages) and page-level replacements.
 * All placeholders must be found or the operation fails atomically.
 */
public final class TemplateReplaceRequest {
    @JsonProperty("replacements")
    private final List<TemplateReplacement> replacements;
    @JsonProperty("pageIndex")
    private final Integer pageIndex;
    @JsonProperty("reflowPreset")
    private final ReflowPreset reflowPreset;

    @JsonCreator
    public TemplateReplaceRequest(
            @JsonProperty("replacements") List<TemplateReplacement> replacements,
            @JsonProperty("pageIndex") Integer pageIndex,
            @JsonProperty("reflowPreset") ReflowPreset reflowPreset
    ) {
        this.replacements = replacements != null ? List.copyOf(replacements) : List.of();
        this.pageIndex = pageIndex;
        this.reflowPreset = reflowPreset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<TemplateReplacement> replacements() {
        return replacements;
    }

    public Integer pageIndex() {
        return pageIndex;
    }

    public ReflowPreset reflowPreset() {
        return reflowPreset;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        TemplateReplaceRequest that = (TemplateReplaceRequest) obj;
        return Objects.equals(this.replacements, that.replacements) &&
                Objects.equals(this.pageIndex, that.pageIndex) &&
                Objects.equals(this.reflowPreset, that.reflowPreset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replacements, pageIndex, reflowPreset);
    }

    @Override
    public String toString() {
        return "TemplateReplaceRequest[" +
                "replacements=" + replacements + ", " +
                "pageIndex=" + pageIndex + ", " +
                "reflowPreset=" + reflowPreset + ']';
    }

    public static class Builder {
        private final List<TemplateReplacement> replacements = new ArrayList<>();
        private Integer pageIndex = null;
        private ReflowPreset reflowPreset = null;

        /**
         * Sets the page index for page-specific replacements.
         * If not set, replacements apply to all pages.
         */
        public Builder pageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        /**
         * Sets the reflow preset for text fitting behavior.
         */
        public Builder reflowPreset(ReflowPreset reflowPreset) {
            this.reflowPreset = reflowPreset;
            return this;
        }

        /**
         * Adds a replacement to the request.
         */
        public Builder addReplacement(TemplateReplacement replacement) {
            this.replacements.add(replacement);
            return this;
        }

        /**
         * Adds a text replacement with optional formatting via chained methods.
         * <p>Example:
         * <pre>{@code
         * builder.replace("{{name}}", "John")
         *        .withFont("Helvetica", 12)
         *        .withColor(255, 0, 0)
         *        .replace("{{title}}", "Manager")
         *        .build();
         * }</pre>
         */
        public ReplacementBuilder replace(String placeholder, String text) {
            return new ReplacementBuilder(this, placeholder, text);
        }

        public TemplateReplaceRequest build() {
            return new TemplateReplaceRequest(replacements, pageIndex, reflowPreset);
        }
    }

    /**
     * Fluent builder for a single replacement with optional formatting.
     * Allows chaining font and color settings before continuing to the next replacement.
     */
    public static class ReplacementBuilder {
        private final Builder parent;
        private final String placeholder;
        private final String text;
        private Font font;
        private Color color;

        ReplacementBuilder(Builder parent, String placeholder, String text) {
            this.parent = parent;
            this.placeholder = placeholder;
            this.text = text;
        }

        /**
         * Sets the font for this replacement.
         */
        public ReplacementBuilder withFont(String name, double size) {
            this.font = new Font(name, size);
            return this;
        }

        /**
         * Sets the font for this replacement.
         */
        public ReplacementBuilder withFont(Font font) {
            this.font = font;
            return this;
        }

        /**
         * Sets the color for this replacement.
         */
        public ReplacementBuilder withColor(int r, int g, int b) {
            this.color = new Color(r, g, b);
            return this;
        }

        /**
         * Sets the color for this replacement.
         */
        public ReplacementBuilder withColor(Color color) {
            this.color = color;
            return this;
        }

        /**
         * Adds the current replacement and starts a new one.
         */
        public ReplacementBuilder replace(String placeholder, String text) {
            commit();
            return new ReplacementBuilder(parent, placeholder, text);
        }

        /**
         * Sets the page index for page-specific replacements.
         */
        public Builder pageIndex(Integer pageIndex) {
            commit();
            return parent.pageIndex(pageIndex);
        }

        /**
         * Sets the reflow preset for text fitting behavior.
         */
        public Builder reflowPreset(ReflowPreset reflowPreset) {
            commit();
            return parent.reflowPreset(reflowPreset);
        }

        /**
         * Builds the request with all replacements.
         */
        public TemplateReplaceRequest build() {
            commit();
            return parent.build();
        }

        private void commit() {
            parent.replacements.add(new TemplateReplacement(placeholder, text, font, color));
        }
    }
}
