package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Response from a redaction operation indicating the results of the redaction.
 */
public final class RedactResponse {
    @JsonProperty("count")
    private final int count;
    @JsonProperty("success")
    private final boolean success;
    @JsonProperty("warnings")
    private final List<String> warnings;

    @JsonCreator
    public RedactResponse(
            @JsonProperty("count") int count,
            @JsonProperty("success") boolean success,
            @JsonProperty("warnings") List<String> warnings
    ) {
        this.count = count;
        this.success = success;
        this.warnings = warnings != null ? List.copyOf(warnings) : List.of();
    }

    public int count() {
        return count;
    }

    public boolean success() {
        return success;
    }

    public List<String> warnings() {
        return warnings;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        RedactResponse that = (RedactResponse) obj;
        return this.count == that.count &&
                this.success == that.success &&
                Objects.equals(this.warnings, that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, success, warnings);
    }

    @Override
    public String toString() {
        return "RedactResponse[" +
                "count=" + count + ", " +
                "success=" + success + ", " +
                "warnings=" + warnings + ']';
    }
}
