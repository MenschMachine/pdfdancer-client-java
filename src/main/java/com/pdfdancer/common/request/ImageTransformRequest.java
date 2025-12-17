package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.Size;

/**
 * Request for transforming an image in a PDF document.
 * Supports replace, scale, rotate, crop, opacity, and flip operations.
 */
public final class ImageTransformRequest {

    /**
     * Types of image transformations supported.
     */
    public enum TransformType {
        /** Replace image content while keeping position. */
        REPLACE,
        /** Scale/resize the image. */
        SCALE,
        /** Rotate the image. */
        ROTATE,
        /** Crop the image by trimming edges. */
        CROP,
        /** Set the opacity/transparency of the image. */
        OPACITY,
        /** Flip the image horizontally or vertically. */
        FLIP
    }

    /**
     * Direction for flip operations.
     */
    public enum FlipDirection {
        /** Flip horizontally (mirror left-right). */
        HORIZONTAL,
        /** Flip vertically (mirror top-bottom). */
        VERTICAL,
        /** Flip both horizontally and vertically. */
        BOTH
    }

    @JsonProperty("objectRef")
    private final ObjectRef objectRef;

    @JsonProperty("transformType")
    private final TransformType transformType;

    @JsonProperty("newImage")
    private final Image newImage;

    @JsonProperty("scaleFactor")
    private final Double scaleFactor;

    @JsonProperty("targetSize")
    private final Size targetSize;

    @JsonProperty("preserveAspectRatio")
    private final Boolean preserveAspectRatio;

    @JsonProperty("rotationAngle")
    private final Double rotationAngle;

    @JsonProperty("cropLeft")
    private final Integer cropLeft;

    @JsonProperty("cropTop")
    private final Integer cropTop;

    @JsonProperty("cropRight")
    private final Integer cropRight;

    @JsonProperty("cropBottom")
    private final Integer cropBottom;

    @JsonProperty("opacity")
    private final Double opacity;

    @JsonProperty("flipDirection")
    private final FlipDirection flipDirection;

    @JsonCreator
    public ImageTransformRequest(
            @JsonProperty("objectRef") ObjectRef objectRef,
            @JsonProperty("transformType") TransformType transformType,
            @JsonProperty("newImage") Image newImage,
            @JsonProperty("scaleFactor") Double scaleFactor,
            @JsonProperty("targetSize") Size targetSize,
            @JsonProperty("preserveAspectRatio") Boolean preserveAspectRatio,
            @JsonProperty("rotationAngle") Double rotationAngle,
            @JsonProperty("cropLeft") Integer cropLeft,
            @JsonProperty("cropTop") Integer cropTop,
            @JsonProperty("cropRight") Integer cropRight,
            @JsonProperty("cropBottom") Integer cropBottom,
            @JsonProperty("opacity") Double opacity,
            @JsonProperty("flipDirection") FlipDirection flipDirection
    ) {
        this.objectRef = objectRef;
        this.transformType = transformType;
        this.newImage = newImage;
        this.scaleFactor = scaleFactor;
        this.targetSize = targetSize;
        this.preserveAspectRatio = preserveAspectRatio;
        this.rotationAngle = rotationAngle;
        this.cropLeft = cropLeft;
        this.cropTop = cropTop;
        this.cropRight = cropRight;
        this.cropBottom = cropBottom;
        this.opacity = opacity;
        this.flipDirection = flipDirection;
    }

    public static Builder builder(ObjectRef objectRef) {
        return new Builder(objectRef);
    }

    public ObjectRef objectRef() {
        return objectRef;
    }

    public TransformType transformType() {
        return transformType;
    }

    public Image newImage() {
        return newImage;
    }

    public Double scaleFactor() {
        return scaleFactor;
    }

    public Size targetSize() {
        return targetSize;
    }

    public Boolean preserveAspectRatio() {
        return preserveAspectRatio;
    }

    public Double rotationAngle() {
        return rotationAngle;
    }

    public Integer cropLeft() {
        return cropLeft;
    }

    public Integer cropTop() {
        return cropTop;
    }

    public Integer cropRight() {
        return cropRight;
    }

    public Integer cropBottom() {
        return cropBottom;
    }

    public Double opacity() {
        return opacity;
    }

    public FlipDirection flipDirection() {
        return flipDirection;
    }

    public static class Builder {
        private final ObjectRef objectRef;
        private TransformType transformType;
        private Image newImage;
        private Double scaleFactor;
        private Size targetSize;
        private Boolean preserveAspectRatio;
        private Double rotationAngle;
        private Integer cropLeft;
        private Integer cropTop;
        private Integer cropRight;
        private Integer cropBottom;
        private Double opacity;
        private FlipDirection flipDirection;

        private Builder(ObjectRef objectRef) {
            this.objectRef = objectRef;
        }

        public Builder replace(Image newImage) {
            this.transformType = TransformType.REPLACE;
            this.newImage = newImage;
            return this;
        }

        public Builder scale(double scaleFactor) {
            this.transformType = TransformType.SCALE;
            this.scaleFactor = scaleFactor;
            return this;
        }

        public Builder scaleTo(Size targetSize) {
            return scaleTo(targetSize, true);
        }

        public Builder scaleTo(Size targetSize, boolean preserveAspectRatio) {
            this.transformType = TransformType.SCALE;
            this.targetSize = targetSize;
            this.preserveAspectRatio = preserveAspectRatio;
            return this;
        }

        public Builder scaleTo(double width, double height) {
            return scaleTo(new Size(width, height), false);
        }

        public Builder scaleTo(double width, double height, boolean preserveAspectRatio) {
            return scaleTo(new Size(width, height), preserveAspectRatio);
        }

        public Builder rotate(double angle) {
            this.transformType = TransformType.ROTATE;
            this.rotationAngle = angle;
            return this;
        }

        public Builder crop(int left, int top, int right, int bottom) {
            this.transformType = TransformType.CROP;
            this.cropLeft = left;
            this.cropTop = top;
            this.cropRight = right;
            this.cropBottom = bottom;
            return this;
        }

        public Builder opacity(double opacity) {
            this.transformType = TransformType.OPACITY;
            this.opacity = opacity;
            return this;
        }

        public Builder flip(FlipDirection direction) {
            this.transformType = TransformType.FLIP;
            this.flipDirection = direction;
            return this;
        }

        public Builder flipHorizontal() {
            return flip(FlipDirection.HORIZONTAL);
        }

        public Builder flipVertical() {
            return flip(FlipDirection.VERTICAL);
        }

        public Builder flipBoth() {
            return flip(FlipDirection.BOTH);
        }

        public ImageTransformRequest build() {
            if (transformType == null) {
                throw new IllegalStateException("Transform type must be specified");
            }
            return new ImageTransformRequest(
                    objectRef,
                    transformType,
                    newImage,
                    scaleFactor,
                    targetSize,
                    preserveAspectRatio,
                    rotationAngle,
                    cropLeft,
                    cropTop,
                    cropRight,
                    cropBottom,
                    opacity,
                    flipDirection
            );
        }
    }
}
