package com.pdfdancer.common.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Objects;

/**
 * Immutable record containing comprehensive image file information.
 * This record encapsulates image data, format details, and dimensional properties
 * extracted from image files, providing all necessary information for PDF image
 * integration and processing operations.
 */
public final class ImageInfo {
    private final byte[] bytes;
    private final String format;
    private final int width;
    private final int height;

    /**
     *
     */
    public ImageInfo(
            /**
             * Raw image file data as byte array.
             */
            byte[] bytes,

            /**
             * Image format name (e.g., "JPEG", "PNG", "GIF").
             */
            String format,

            /**
             * Image width in pixels.
             */
            int width,

            /**
             * Image height in pixels.
             */
            int height
    ) {
        this.bytes = bytes;
        this.format = format;
        this.width = width;
        this.height = height;
    }

    /**
     * Reads and analyzes an image file to extract complete image information.
     * This factory method processes an image file to determine its format, dimensions,
     * and binary content, creating a complete ImageInfo record suitable for PDF
     * integration operations.
     *
     * @param file the image file to analyze and read
     * @return ImageInfo record containing all extracted image data and metadata
     * @throws IOException              if the file cannot be read or accessed
     * @throws IllegalArgumentException if the file is not a supported image format
     */
    public static ImageInfo read(File file) throws IOException {
        // Read image into byte array (raw file bytes)
        byte[] imageBytes = Files.readAllBytes(file.toPath());

        // Load BufferedImage
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IllegalArgumentException("Not a supported image file: " + file);
        }

        int width = img.getWidth();
        int height = img.getHeight();
        int bufferedType = img.getType(); // e.g., BufferedImage.TYPE_INT_RGB

        // Get image format (JPEG, PNG, etc.)
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                String formatName = reader.getFormatName();
                reader.dispose();
                return new ImageInfo(imageBytes, formatName, width, height);
            } else {
                throw new IllegalArgumentException("Not a supported image file: " + file);
            }
        }
    }

    public byte[] bytes() {
        return bytes;
    }

    public String format() {
        return format;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ImageInfo) obj;
        return Objects.equals(this.bytes, that.bytes) &&
                Objects.equals(this.format, that.format) &&
                this.width == that.width &&
                this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes, format, width, height);
    }

    @Override
    public String toString() {
        return "ImageInfo[" +
                "bytes=" + bytes + ", " +
                "format=" + format + ", " +
                "width=" + width + ", " +
                "height=" + height + ']';
    }

}