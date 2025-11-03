package com.tfc.pdf.pdfdancer.api.common.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * Immutable record containing comprehensive image file information.
 * This record encapsulates image data, format details, and dimensional properties
 * extracted from image files, providing all necessary information for PDF image
 * integration and processing operations.
 */
public record ImageInfo(
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

    /**
     * Reads and analyzes an image file to extract complete image information.
     * This factory method processes an image file to determine its format, dimensions,
     * and binary content, creating a complete ImageInfo record suitable for PDF
     * integration operations.
     * 
     * @param file the image file to analyze and read
     * @return ImageInfo record containing all extracted image data and metadata
     * @throws IOException if the file cannot be read or accessed
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
}