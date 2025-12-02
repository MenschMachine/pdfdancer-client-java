package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a single redaction target specifying what content to redact.
 * Each target identifies an object by its internal ID.
 */
public final class RedactTarget {
    @JsonProperty("id")
    private final String id;
    @JsonProperty("replacement")
    private final String replacement;

    @JsonCreator
    public RedactTarget(
            @JsonProperty("id") String id,
            @JsonProperty("replacement") String replacement
    ) {
        this.id = id;
        this.replacement = replacement;
    }

    public String id() {
        return id;
    }

    public String replacement() {
        return replacement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        RedactTarget that = (RedactTarget) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, replacement);
    }

    @Override
    public String toString() {
        return "RedactTarget[" +
                "id=" + id + ", " +
                "replacement=" + replacement + ']';
    }
}
