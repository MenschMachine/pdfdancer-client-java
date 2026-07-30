package com.pdfdancer.common.response;

public enum ReadingUnitRole {
    HEADING, PARAGRAPH, LIST, TABLE, CAPTION, CALLOUT, PREFORMATTED_TEXT,
    PAGE_HEADER, PAGE_FOOTER, PAGE_NUMBER, FOOTNOTE, WATERMARK, UNKNOWN;

    public static ReadingUnitRole fromValue(String value) {
        if (value == null) return UNKNOWN;
        try { return valueOf(value); } catch (IllegalArgumentException ignored) { return UNKNOWN; }
    }
}
