package com.pdfdancer.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfAffineTransformTest {

    @Test
    void builderStartsWithIdentity() {
        assertArrayEquals(
                new double[]{1, 0, 0, 1, 0, 0},
                PdfAffineTransform.builder().build().toPdfMatrix());
    }

    @Test
    void operationsApplyToPointsInInvocationOrder() {
        PdfAffineTransform scaleThenTranslate = PdfAffineTransform.builder()
                .scale(20, 10)
                .translate(3, -2)
                .build();
        PdfAffineTransform translateThenScale = PdfAffineTransform.builder()
                .translate(3, -2)
                .scale(20, 10)
                .build();

        assertArrayEquals(new double[]{20, 0, 0, 10, 3, -2},
                scaleThenTranslate.toPdfMatrix());
        assertArrayEquals(new double[]{20, 0, 0, 10, 60, -20},
                translateThenScale.toPdfMatrix());
    }

    @Test
    void builderSupportsRotationAndShear() {
        PdfAffineTransform rotation = PdfAffineTransform.builder()
                .rotateDegrees(90)
                .build();
        PdfAffineTransform shear = PdfAffineTransform.builder()
                .shear(0.25, -0.5)
                .build();

        assertArrayEquals(new double[]{0, 1, -1, 0, 0, 0},
                rotation.toPdfMatrix(), 1.0e-12);
        assertArrayEquals(new double[]{1, -0.5, 0.25, 1, 0, 0},
                shear.toPdfMatrix());
    }

    @Test
    void exactPdfMatrixInputAndOutputAreDefensivelyCopied() {
        double[] coefficients = {20, 0, 5, 10, 3, -2};
        PdfAffineTransform transform = PdfAffineTransform.fromPdfMatrix(coefficients);
        coefficients[0] = 999;

        double[] firstOutput = transform.toPdfMatrix();
        double[] secondOutput = transform.toPdfMatrix();
        firstOutput[1] = 999;

        assertArrayEquals(new double[]{20, 0, 5, 10, 3, -2}, secondOutput);
        assertNotSame(firstOutput, secondOutput);
        assertEquals(PdfAffineTransform.fromPdfMatrix(secondOutput), transform);
    }

    @Test
    void rejectsInvalidCoefficientsAndOperationArguments() {
        assertThrows(NullPointerException.class, () -> PdfAffineTransform.fromPdfMatrix(null));
        assertThrows(IllegalArgumentException.class,
                () -> PdfAffineTransform.fromPdfMatrix(new double[]{1, 0}));
        assertThrows(IllegalArgumentException.class,
                () -> PdfAffineTransform.fromPdfMatrix(new double[]{1, 0, 0, 1, Double.NaN, 0}));
        assertThrows(IllegalArgumentException.class,
                () -> PdfAffineTransform.builder().scale(Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class,
                () -> PdfAffineTransform.builder().rotateDegrees(Double.NaN));
    }
}
