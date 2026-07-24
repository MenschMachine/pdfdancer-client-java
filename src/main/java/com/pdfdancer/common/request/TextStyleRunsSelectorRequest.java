package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStyleRunsSelectorRequest {
    @JsonProperty("where")
    private final TextStyleRunFilterRequest where;
    @JsonProperty("maxMatches")
    private final Integer maxMatches;

    @JsonCreator
    public TextStyleRunsSelectorRequest(@JsonProperty("where") TextStyleRunFilterRequest where,
                                        @JsonProperty("maxMatches") Integer maxMatches) {
        this.where = where;
        this.maxMatches = maxMatches;
    }

    public TextStyleRunFilterRequest where() { return where; }
    public Integer maxMatches() { return maxMatches; }

    public TextStyleRunsSelectorRequest validated() {
        if (where == null) {
            throw new IllegalArgumentException("runs.where must not be null");
        }
        where.validated();
        if (maxMatches != null && maxMatches <= 0) {
            throw new IllegalArgumentException("runs.maxMatches must be positive");
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStyleRunsSelectorRequest) obj;
        return Objects.equals(where, that.where) &&
                Objects.equals(maxMatches, that.maxMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(where, maxMatches);
    }

    @Override
    public String toString() {
        return "TextStyleRunsSelectorRequest[where=" + where + ", maxMatches=" + maxMatches + ']';
    }
}
