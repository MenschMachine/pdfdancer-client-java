package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStyleNumericFilterRequest {
    @JsonProperty("eq")
    private final Double eq;
    @JsonProperty("tolerance")
    private final Double tolerance;

    @JsonCreator
    public TextStyleNumericFilterRequest(@JsonProperty("eq") Double eq,
                                         @JsonProperty("tolerance") Double tolerance) {
        this.eq = eq;
        this.tolerance = tolerance;
    }

    public static TextStyleNumericFilterRequest eq(double eq) {
        return new TextStyleNumericFilterRequest(eq, null).validated();
    }

    public static TextStyleNumericFilterRequest eq(double eq, double tolerance) {
        return new TextStyleNumericFilterRequest(eq, tolerance).validated();
    }

    public Double eq() { return eq; }
    public Double tolerance() { return tolerance; }

    public TextStyleNumericFilterRequest validated() {
        if (eq == null || !Double.isFinite(eq)) {
            throw new IllegalArgumentException("numeric filter eq must be finite");
        }
        if (tolerance != null && (!Double.isFinite(tolerance) || tolerance < 0.0)) {
            throw new IllegalArgumentException("numeric filter tolerance must be finite and >= 0.0");
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStyleNumericFilterRequest) obj;
        return Objects.equals(eq, that.eq) &&
                Objects.equals(tolerance, that.tolerance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eq, tolerance);
    }

    @Override
    public String toString() {
        return "TextStyleNumericFilterRequest[eq=" + eq + ", tolerance=" + tolerance + ']';
    }
}
