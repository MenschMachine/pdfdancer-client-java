package com.tfc.pdf.pdfdancer.api.common.util;

/**
 * The 14 standard PDF fonts that are guaranteed to be available in all PDF readers.
 * These fonts do not need to be embedded in the PDF document.
 *
 * <p><b>Serif fonts (Times family):</b><br>
 * - TIMES_ROMAN: Standard Times Roman font<br>
 * - TIMES_BOLD: Bold version of Times Roman<br>
 * - TIMES_ITALIC: Italic version of Times Roman<br>
 * - TIMES_BOLD_ITALIC: Bold and italic version of Times Roman
 *
 * <p><b>Sans-serif fonts (Helvetica family):</b><br>
 * - HELVETICA: Standard Helvetica font<br>
 * - HELVETICA_BOLD: Bold version of Helvetica<br>
 * - HELVETICA_OBLIQUE: Oblique (italic) version of Helvetica<br>
 * - HELVETICA_BOLD_OBLIQUE: Bold and oblique version of Helvetica
 *
 * <p><b>Monospace fonts (Courier family):</b><br>
 * - COURIER: Standard Courier font<br>
 * - COURIER_BOLD: Bold version of Courier<br>
 * - COURIER_OBLIQUE: Oblique (italic) version of Courier<br>
 * - COURIER_BOLD_OBLIQUE: Bold and oblique version of Courier
 *
 * <p><b>Symbol and decorative fonts:</b><br>
 * - SYMBOL: Symbol font for mathematical and special characters<br>
 * - ZAPF_DINGBATS: Zapf Dingbats font for decorative symbols
 */
public enum StandardFonts {
    // Serif fonts (Times family)
    TIMES_ROMAN("Times-Roman"),
    TIMES_BOLD("Times-Bold"),
    TIMES_ITALIC("Times-Italic"),
    TIMES_BOLD_ITALIC("Times-BoldItalic"),

    // Sans-serif fonts (Helvetica family)
    HELVETICA("Helvetica"),
    HELVETICA_BOLD("Helvetica-Bold"),
    HELVETICA_OBLIQUE("Helvetica-Oblique"),
    HELVETICA_BOLD_OBLIQUE("Helvetica-BoldOblique"),

    // Monospace fonts (Courier family)
    COURIER("Courier"),
    COURIER_BOLD("Courier-Bold"),
    COURIER_OBLIQUE("Courier-Oblique"),
    COURIER_BOLD_OBLIQUE("Courier-BoldOblique"),

    // Symbol and decorative fonts
    SYMBOL("Symbol"),
    ZAPF_DINGBATS("ZapfDingbats");

    private final String fontName;

    StandardFonts(String fontName) {
        this.fontName = fontName;
    }

    public String getFontName() {
        return fontName;
    }

    /**
     * Checks whether a given font name matches one of the standard PDF fonts.
     *
     * @param name The font name (case-insensitive, may include subset prefixes)
     * @return true if the font name corresponds to a standard PDF font
     */
    public static boolean isStandardFont(String name) {
        if (name == null) return false;
        String normalized = name.replaceAll("^[A-Z]{6}\\+", ""); // remove subset prefix
        for (StandardFonts font : values()) {
            if (font.getFontName().equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }
}