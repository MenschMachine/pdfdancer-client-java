package com.tfc.pdf.pdfdancer.api.common.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * Represents PDF page dimensions, supporting both standard sizes and custom dimensions.
 * Dimensions are specified in points (1/72 inch).
 */
@SuppressWarnings("unused")
public class PageSize {
    // ISO A Series
    /**
     * ISO A0 size (841mm x 1189mm = 2384 x 3370 points)
     */
    public static final PageSize A0 = new PageSize("A0", 2384f, 3370f);
    /**
     * ISO A1 size (594mm x 841mm = 1684 x 2384 points)
     */
    public static final PageSize A1 = new PageSize("A1", 1684f, 2384f);
    /**
     * ISO A2 size (420mm x 594mm = 1191 x 1684 points)
     */
    public static final PageSize A2 = new PageSize("A2", 1191f, 1684f);
    /**
     * ISO A3 size (297mm x 420mm = 842 x 1191 points)
     */
    public static final PageSize A3 = new PageSize("A3", 842f, 1191f);
    /**
     * ISO A4 size (210mm x 297mm = 595 x 842 points)
     */
    public static final PageSize A4 = new PageSize("A4", 595f, 842f);
    /**
     * ISO A5 size (148mm x 210mm = 420 x 595 points)
     */
    public static final PageSize A5 = new PageSize("A5", 420f, 595f);
    /**
     * ISO A6 size (105mm x 148mm = 298 x 420 points)
     */
    public static final PageSize A6 = new PageSize("A6", 298f, 420f);
    // ISO B Series
    /**
     * ISO B4 size (250mm x 353mm = 709 x 1001 points)
     */
    public static final PageSize B4 = new PageSize("B4", 709f, 1001f);
    /**
     * ISO B5 size (176mm x 250mm = 499 x 709 points)
     */
    public static final PageSize B5 = new PageSize("B5", 499f, 709f);
    // US/North American sizes
    /**
     * US Letter size (8.5" x 11" = 612 x 792 points)
     */
    public static final PageSize LETTER = new PageSize("LETTER", 612f, 792f);
    /**
     * US Legal size (8.5" x 14" = 612 x 1008 points)
     */
    public static final PageSize LEGAL = new PageSize("LEGAL", 612f, 1008f);
    /**
     * US Tabloid/Ledger size (11" x 17" = 792 x 1224 points)
     */
    public static final PageSize TABLOID = new PageSize("TABLOID", 792f, 1224f);
    /**
     * US Executive size (7.25" x 10.5" = 522 x 756 points)
     */
    public static final PageSize EXECUTIVE = new PageSize("EXECUTIVE", 522f, 756f);
    /**
     * Postcard size (4" x 6" = 288 x 432 points)
     */
    public static final PageSize POSTCARD = new PageSize("POSTCARD", 288f, 432f);
    /**
     * Index Card 3x5 size (3" x 5" = 216 x 360 points)
     */
    public static final PageSize INDEX_3X5 = new PageSize("INDEX_3X5", 216f, 360f);
    private String name;
    private double width;
    private double height;
    /**
     * Default constructor for serialization.
     */
    public PageSize() {
    }
    /**
     * Creates a page size with specified dimensions.
     *
     * @param width  the width dimension
     * @param height the height dimension
     */
    public PageSize(double width, double height) {
        this(null, width, height);
    }
    /**
     * Creates a page size with name and dimensions.
     *
     * @param name   the name of the size (null for custom sizes)
     * @param width  the width dimension
     * @param height the height dimension
     */
    public PageSize(String name, double width, double height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }
    /**
     * Creates a custom page size with specified dimensions in points.
     *
     * @param width  width in points (1/72 inch)
     * @param height height in points (1/72 inch)
     * @return custom PageSize instance
     */
    public static PageSize custom(double width, double height) {
        return new PageSize(null, width, height);
    }
    public static PageSize of(double pageWidth, double pageHeight) {
        // Allow small floating point differences (e.g., from rounding or rotation)
        final double tolerance = 0.5;
        // List of known standard sizes
        PageSize[] standardSizes = {
                A0, A1, A2, A3, A4, A5, A6,
                B4, B5,
                LETTER, LEGAL, TABLOID, EXECUTIVE,
                POSTCARD, INDEX_3X5
        };
        for (PageSize standard : standardSizes) {
            if (Math.abs(standard.getWidth() - pageWidth) < tolerance &&
                    Math.abs(standard.getHeight() - pageHeight) < tolerance) {
                return standard;
            }
            // Handle rotated pages (width/height swapped)
            if (Math.abs(standard.getWidth() - pageHeight) < tolerance &&
                    Math.abs(standard.getHeight() - pageWidth) < tolerance) {
                return standard;
            }
        }
        // If not matching any standard, return a custom one
        return PageSize.custom(pageWidth, pageHeight);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getWidth() {
        return width;
    }
    public void setWidth(double width) {
        this.width = width;
    }
    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }
    @JsonIgnore
    public boolean isStandard() {
        return name != null;
    }
    @JsonIgnore
    public boolean isCustom() {
        return name == null;
    }
}
