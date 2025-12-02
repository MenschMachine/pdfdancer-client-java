package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request for batch redaction of PDF content.
 * Text content is replaced with a replacement string, while images and paths
 * are replaced with solid color placeholder rectangles.
 */
public final class RedactRequest {
    @JsonProperty("targets")
    private final List<RedactTarget> targets;
    @JsonProperty("defaultReplacement")
    private final String defaultReplacement;
    @JsonProperty("placeholderColor")
    private final Color placeholderColor;

    @JsonCreator
    public RedactRequest(
            @JsonProperty("targets") List<RedactTarget> targets,
            @JsonProperty("defaultReplacement") String defaultReplacement,
            @JsonProperty("placeholderColor") Color placeholderColor
    ) {
        this.targets = targets != null ? List.copyOf(targets) : List.of();
        this.defaultReplacement = defaultReplacement;
        this.placeholderColor = placeholderColor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<RedactTarget> targets() {
        return targets;
    }

    public String defaultReplacement() {
        return defaultReplacement;
    }

    public Color placeholderColor() {
        return placeholderColor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        RedactRequest that = (RedactRequest) obj;
        return Objects.equals(this.targets, that.targets) &&
                Objects.equals(this.defaultReplacement, that.defaultReplacement) &&
                Objects.equals(this.placeholderColor, that.placeholderColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targets, defaultReplacement, placeholderColor);
    }

    @Override
    public String toString() {
        return "RedactRequest[" +
                "targets=" + targets + ", " +
                "defaultReplacement=" + defaultReplacement + ", " +
                "placeholderColor=" + placeholderColor + ']';
    }

    public static class Builder {
        private final List<RedactTarget> targets = new ArrayList<>();
        private String defaultReplacement = "[REDACTED]";
        private Color placeholderColor = Color.BLACK;

        public Builder defaultReplacement(String defaultReplacement) {
            this.defaultReplacement = defaultReplacement;
            return this;
        }

        public Builder placeholderColor(Color placeholderColor) {
            this.placeholderColor = placeholderColor;
            return this;
        }

        public Builder addTarget(RedactTarget target) {
            this.targets.add(target);
            return this;
        }

        public Builder addTargetById(String id) {
            return addTargetById(id, null);
        }

        public Builder addTargetById(String id, String replacement) {
            this.targets.add(new RedactTarget(id, replacement));
            return this;
        }

        public RedactRequest build() {
            return new RedactRequest(targets, defaultReplacement, placeholderColor);
        }
    }
}
