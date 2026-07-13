package com.pdfdancer.common.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable two-dimensional affine transformation using the PDF six-number
 * matrix convention {@code [a, b, c, d, e, f]}.
 *
 * <p>The transform maps a point {@code (x, y)} to:</p>
 * <pre>
 * x' = a * x + c * y + e
 * y' = b * x + d * y + f
 * </pre>
 *
 * <p>The fluent builder starts with the identity transform. Operations are
 * applied to points in invocation order.</p>
 */
public final class PdfAffineTransform {
    private static final int PDF_MATRIX_SIZE = 6;

    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final double e;
    private final double f;

    private PdfAffineTransform(double a, double b, double c, double d, double e, double f) {
        validateFinite(a, b, c, d, e, f);
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    /**
     * Creates a transform from coefficients in PDF {@code [a,b,c,d,e,f]} order.
     *
     * @param coefficients exactly six finite coefficients
     * @return the represented affine transform
     */
    public static PdfAffineTransform fromPdfMatrix(double[] coefficients) {
        Objects.requireNonNull(coefficients, "coefficients");
        if (coefficients.length != PDF_MATRIX_SIZE) {
            throw new IllegalArgumentException("PDF affine matrix must contain exactly 6 coefficients");
        }
        return new PdfAffineTransform(
                coefficients[0], coefficients[1], coefficients[2],
                coefficients[3], coefficients[4], coefficients[5]);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a defensive copy in PDF {@code [a,b,c,d,e,f]} order.
     */
    public double[] toPdfMatrix() {
        return new double[]{a, b, c, d, e, f};
    }

    public double a() { return a; }
    public double b() { return b; }
    public double c() { return c; }
    public double d() { return d; }
    public double e() { return e; }
    public double f() { return f; }

    private PdfAffineTransform followedBy(PdfAffineTransform next) {
        return new PdfAffineTransform(
                next.a * a + next.c * b,
                next.b * a + next.d * b,
                next.a * c + next.c * d,
                next.b * c + next.d * d,
                next.a * e + next.c * f + next.e,
                next.b * e + next.d * f + next.f
        );
    }

    private static void validateFinite(double... coefficients) {
        for (double coefficient : coefficients) {
            if (!Double.isFinite(coefficient)) {
                throw new IllegalArgumentException("PDF affine matrix coefficients must be finite");
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PdfAffineTransform that)) return false;
        return Double.compare(a, that.a) == 0
                && Double.compare(b, that.b) == 0
                && Double.compare(c, that.c) == 0
                && Double.compare(d, that.d) == 0
                && Double.compare(e, that.e) == 0
                && Double.compare(f, that.f) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toPdfMatrix());
    }

    @Override
    public String toString() {
        return "PdfAffineTransform" + Arrays.toString(toPdfMatrix());
    }

    public static final class Builder {
        private PdfAffineTransform transform = new PdfAffineTransform(1, 0, 0, 1, 0, 0);

        private Builder() {
        }

        public Builder scale(double scaleX, double scaleY) {
            transform = transform.followedBy(new PdfAffineTransform(scaleX, 0, 0, scaleY, 0, 0));
            return this;
        }

        /**
         * Shears X by Y and Y by X using the matrix
         * {@code [1, shearY, shearX, 1, 0, 0]}.
         */
        public Builder shear(double shearX, double shearY) {
            transform = transform.followedBy(new PdfAffineTransform(1, shearY, shearX, 1, 0, 0));
            return this;
        }

        public Builder rotateDegrees(double degrees) {
            if (!Double.isFinite(degrees)) {
                throw new IllegalArgumentException("rotation degrees must be finite");
            }
            double radians = Math.toRadians(degrees);
            double cosine = Math.cos(radians);
            double sine = Math.sin(radians);
            transform = transform.followedBy(new PdfAffineTransform(
                    cosine, sine, -sine, cosine, 0, 0));
            return this;
        }

        public Builder translate(double translateX, double translateY) {
            transform = transform.followedBy(new PdfAffineTransform(1, 0, 0, 1, translateX, translateY));
            return this;
        }

        public PdfAffineTransform build() {
            return transform;
        }
    }
}
