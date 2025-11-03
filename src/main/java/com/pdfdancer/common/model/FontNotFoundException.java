package com.pdfdancer.common.model;

public class FontNotFoundException extends IllegalArgumentException {

    private final String fontName;

    public FontNotFoundException(String fontName) {
        super("Font not found: " + fontName);
        this.fontName = fontName;
    }

    @Override
    public String toString() {
        return "FontNotFoundException{" +
                "fontName='" + fontName + '\'' +
                '}';
    }

    public String getFont() {
        return fontName;
    }
}
