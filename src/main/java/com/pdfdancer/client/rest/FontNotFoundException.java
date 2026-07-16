package com.pdfdancer.client.rest;

/** Required font is not available to the document session. */
public class FontNotFoundException extends PdfDancerException {
    private final String fontName;
    public FontNotFoundException(String fontName) {
        super("Font not found: " + fontName);
        this.fontName = fontName;
    }
    public String getFont() { return fontName; }
}
