package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;

import java.util.Objects;

/**
 * Represents a single redaction target specifying what content to find and redact.
 * Each target defines an object type and position criteria to search for,
 * with an optional replacement string that overrides the default.
 */
public final class RedactTarget {
    @JsonProperty("objectType")
    private final ObjectType objectType;
    @JsonProperty("position")
    private final Position position;
    @JsonProperty("replacement")
    private final String replacement;

    @JsonCreator
    public RedactTarget(
            @JsonProperty("objectType") ObjectType objectType,
            @JsonProperty("position") Position position,
            @JsonProperty("replacement") String replacement
    ) {
        this.objectType = objectType;
        this.position = position;
        this.replacement = replacement;
    }

    public ObjectType objectType() {
        return objectType;
    }

    public Position position() {
        return position;
    }

    public String replacement() {
        return replacement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        RedactTarget that = (RedactTarget) obj;
        return Objects.equals(this.objectType, that.objectType) &&
                Objects.equals(this.position, that.position) &&
                Objects.equals(this.replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, position, replacement);
    }

    @Override
    public String toString() {
        return "RedactTarget[" +
                "objectType=" + objectType + ", " +
                "position=" + position + ", " +
                "replacement=" + replacement + ']';
    }
}
