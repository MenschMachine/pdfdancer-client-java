package com.tfc.pdf.pdfdancer.api.common.model;

import com.tfc.pdf.pdfdancer.api.common.util.ImageInfo;

import java.io.File;
import java.io.IOException;

/**
 * Represents an image object within a PDF document.
 * This class encapsulates image data, format information, and dimensional properties
 * for embedded or overlaid images in PDF documents. Supports various image formats
 * and provides utilities for loading images from files.
 */
public class Image extends PDFObject {
    /**
     * Image format (e.g., "JPEG", "PNG", "GIF").
     */
    private String format;
    /**
     * Dimensions of the image in PDF coordinate units.
     */
    private Size size;
    /**
     * Raw image data bytes.
     */
    private byte[] data;

    /**
     * Default constructor for serialization frameworks.
     */
    public Image() {
        super();
    }

    /**
     * Creates an image with specified properties and data.
     *
     * @param id       unique identifier for the image
     * @param format   image format (e.g., "JPEG", "PNG")
     * @param size     dimensions of the image
     * @param position location within the PDF document
     * @param data     raw image data bytes
     */
    public Image(String id, String format, Size size, Position position, byte[] data) {
        super(id, position);
        this.format = format;
        this.size = size;
        this.data = data;
    }

    /**
     * Creates an Image object by loading data from a file.
     * This factory method reads image file data, extracts format and dimension
     * information, and creates a complete Image object ready for PDF integration.
     *
     * @param file the image file to load
     * @return a new Image object containing the file's image data and properties
     * @throws IOException if the file cannot be read or is not a valid image
     */
    public static Image fromFile(File file) throws IOException {
        ImageInfo read = ImageInfo.read(file);
        Image image = new Image();
        image.setData(read.bytes());
        image.setSize(new Size(read.width(), read.height()));
        image.setFormat(read.format());
        return image;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the object type for this image.
     *
     * @return ObjectType.IMAGE indicating this is an image object
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.IMAGE;
    }
}