package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
         * Adds a simple text replacement.
         */
        public Builder replace(String placeholder, String text) {
            this.replacements.add(TemplateReplacement.of(placeholder, text));
            return this;
        }

        public TemplateReplaceRequest build() {
            return new TemplateReplaceRequest(replacements, pageIndex, reflowPreset);
        }
    }
}
