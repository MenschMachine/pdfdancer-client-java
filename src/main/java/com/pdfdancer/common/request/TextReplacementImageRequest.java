package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.PdfAffineTransform;

import java.util.Arrays;
import java.util.Objects;

/**
 * Bitmap data and caret-relative geometry for replacing matched decoded text.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextReplacementImageRequest {
    private final byte[] data;
    private final PdfAffineTransform transformation;

    public TextReplacementImageRequest(byte[] data, PdfAffineTransform transformation) {
        this.data = data == null ? null : data.clone();
        this.transformation = transformation;
    }

    @JsonCreator
    private static TextReplacementImageRequest fromJson(
            @JsonProperty("data") byte[] data,
            @JsonProperty("transformationMatrix") double[] transformationMatrix) {
        return new TextReplacementImageRequest(
                data,
                transformationMatrix == null ? null : PdfAffineTransform.fromPdfMatrix(transformationMatrix));
    }

    @JsonProperty("data")
    public byte[] data() {
        return data == null ? null : data.clone();
    }

    public PdfAffineTransform transformation() {
        return transformation;
    }

    @JsonProperty("transformationMatrix")
    private double[] transformationMatrix() {
        return transformation == null ? null : transformation.toPdfMatrix();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TextReplacementImageRequest that)) return false;
        return Arrays.equals(data, that.data)
                && Objects.equals(transformation, that.transformation);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(data) + Objects.hashCode(transformation);
    }
}
