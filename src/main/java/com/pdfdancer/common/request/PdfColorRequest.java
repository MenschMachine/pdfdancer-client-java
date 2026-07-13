package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PdfColorRequest {
    public enum Space {
        rgb,
        cmyk,
        gray
    }

    @JsonProperty("space")
    private final Space space;
    @JsonProperty("components")
    private final List<Double> components;
    @JsonProperty("alpha")
    private final Double alpha;

    @JsonCreator
    public PdfColorRequest(@JsonProperty("space") Space space,
                           @JsonProperty("components") List<Double> components,
                           @JsonProperty("alpha") Double alpha) {
        this.space = space;
        this.components = components == null ? null : List.copyOf(components);
        this.alpha = alpha;
    }

    public static PdfColorRequest rgb(double red, double green, double blue) {
        return new PdfColorRequest(Space.rgb, List.of(red, green, blue), null).validated();
    }

    public static PdfColorRequest cmyk(double cyan, double magenta, double yellow, double black) {
        return new PdfColorRequest(Space.cmyk, List.of(cyan, magenta, yellow, black), null).validated();
    }

    public static PdfColorRequest gray(double gray) {
        return new PdfColorRequest(Space.gray, List.of(gray), null).validated();
    }

    public Space space() { return space; }
    public List<Double> components() { return components; }
    public Double alpha() { return alpha; }

    public PdfColorRequest alpha(double alpha) {
        return new PdfColorRequest(space, components, alpha).validated();
    }

    public PdfColorRequest validated() {
        if (space == null) {
            throw new IllegalArgumentException("color space must not be null");
        }
        if (components == null) {
            throw new IllegalArgumentException("color components must not be null");
        }
        int expectedSize = expectedComponentCount(space);
        if (components.size() != expectedSize) {
            throw new IllegalArgumentException("color space " + space + " requires " + expectedSize + " components");
        }
        for (Double component : components) {
            validateNormalized(component, "color component");
        }
        if (alpha != null) {
            validateNormalized(alpha, "alpha");
        }
        return this;
    }

    private static int expectedComponentCount(Space space) {
        switch (space) {
            case rgb:
                return 3;
            case cmyk:
                return 4;
            case gray:
                return 1;
            default:
                throw new IllegalArgumentException("Unsupported color space: " + space);
        }
    }

    static void validateNormalized(Double value, String name) {
        if (value == null || !Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be finite and between 0.0 and 1.0");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PdfColorRequest) obj;
        return space == that.space &&
                Objects.equals(components, that.components) &&
                Objects.equals(alpha, that.alpha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(space, components, alpha);
    }

    @Override
    public String toString() {
        return "PdfColorRequest[" +
                "space=" + space + ", " +
                "components=" + components + ", " +
                "alpha=" + alpha + ']';
    }
}
