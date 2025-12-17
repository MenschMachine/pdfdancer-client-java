package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.Size;
import com.pdfdancer.common.request.ImageTransformRequest;
import com.pdfdancer.common.request.ImageTransformRequest.FlipDirection;

import java.io.File;
import java.io.IOException;

/**
 * Reference to an image in a PDF document with transformation capabilities.
 */
public class ImageReference extends BaseReference {

    public ImageReference(PDFDancer client, ObjectRef objectRef) {
        super(client, objectRef);
    }

    /**
     * Returns the width of the image from its bounding rect.
     *
     * @return the width, or null if not available
     */
    public Double getWidth() {
        if (objectRef.getPosition() == null || objectRef.getPosition().getBoundingRect() == null) {
            return null;
        }
        return objectRef.getPosition().getBoundingRect().getWidth();
    }

    /**
     * Returns the height of the image from its bounding rect.
     *
     * @return the height, or null if not available
     */
    public Double getHeight() {
        if (objectRef.getPosition() == null || objectRef.getPosition().getBoundingRect() == null) {
            return null;
        }
        return objectRef.getPosition().getBoundingRect().getHeight();
    }

    /**
     * Returns the aspect ratio (width/height) of the image.
     *
     * @return the aspect ratio, or null if dimensions not available
     */
    public Double getAspectRatio() {
        Double width = getWidth();
        Double height = getHeight();
        if (width == null || height == null || height == 0) {
            return null;
        }
        return width / height;
    }

    /**
     * Replaces this image with a new image from a file.
     *
     * @param imageFile the new image file
     * @return true if the replacement was successful
     * @throws IOException if the file cannot be read
     */
    public boolean replace(File imageFile) throws IOException {
        Image newImage = Image.fromFile(imageFile);
        return replace(newImage);
    }

    /**
     * Replaces this image with a new image.
     *
     * @param newImage the new image
     * @return true if the replacement was successful
     */
    public boolean replace(Image newImage) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .replace(newImage)
                .build();
        return client.transformImage(request);
    }

    /**
     * Scales this image by a factor.
     *
     * @param scaleFactor the scale factor (e.g., 0.5 for half size, 2.0 for double size)
     * @return true if the scaling was successful
     */
    public boolean scale(double scaleFactor) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .scale(scaleFactor)
                .build();
        return client.transformImage(request);
    }

    /**
     * Scales this image to a target size, preserving aspect ratio.
     *
     * @param targetSize the target size
     * @return true if the scaling was successful
     */
    public boolean scaleTo(Size targetSize) {
        return scaleTo(targetSize, true);
    }

    /**
     * Scales this image to a target size.
     *
     * @param targetSize          the target size
     * @param preserveAspectRatio whether to preserve aspect ratio
     * @return true if the scaling was successful
     */
    public boolean scaleTo(Size targetSize, boolean preserveAspectRatio) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .scaleTo(targetSize, preserveAspectRatio)
                .build();
        return client.transformImage(request);
    }

    /**
     * Scales this image to a target width and height.
     *
     * @param width  the target width
     * @param height the target height
     * @return true if the scaling was successful
     */
    public boolean scaleTo(double width, double height) {
        return scaleTo(new Size(width, height), false);
    }

    /**
     * Scales this image to a target width and height.
     *
     * @param width               the target width
     * @param height              the target height
     * @param preserveAspectRatio whether to preserve aspect ratio
     * @return true if the scaling was successful
     */
    public boolean scaleTo(double width, double height, boolean preserveAspectRatio) {
        return scaleTo(new Size(width, height), preserveAspectRatio);
    }

    /**
     * Rotates this image by the specified angle.
     *
     * @param angle the rotation angle in degrees (positive = clockwise)
     * @return true if the rotation was successful
     */
    public boolean rotate(double angle) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .rotate(angle)
                .build();
        return client.transformImage(request);
    }

    /**
     * Crops this image by trimming pixels from each edge.
     *
     * @param left   pixels to trim from left edge
     * @param top    pixels to trim from top edge
     * @param right  pixels to trim from right edge
     * @param bottom pixels to trim from bottom edge
     * @return true if the cropping was successful
     */
    public boolean crop(int left, int top, int right, int bottom) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .crop(left, top, right, bottom)
                .build();
        return client.transformImage(request);
    }

    /**
     * Sets the opacity of this image.
     *
     * @param opacity the opacity value (0.0 = fully transparent, 1.0 = fully opaque)
     * @return true if the operation was successful
     */
    public boolean opacity(double opacity) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .opacity(opacity)
                .build();
        return client.transformImage(request);
    }

    /**
     * Flips this image in the specified direction.
     *
     * @param direction the flip direction
     * @return true if the flip was successful
     */
    public boolean flip(FlipDirection direction) {
        ImageTransformRequest request = ImageTransformRequest.builder(objectRef)
                .flip(direction)
                .build();
        return client.transformImage(request);
    }

    /**
     * Flips this image horizontally (mirror left-right).
     *
     * @return true if the flip was successful
     */
    public boolean flipHorizontal() {
        return flip(FlipDirection.HORIZONTAL);
    }

    /**
     * Flips this image vertically (mirror top-bottom).
     *
     * @return true if the flip was successful
     */
    public boolean flipVertical() {
        return flip(FlipDirection.VERTICAL);
    }
}
