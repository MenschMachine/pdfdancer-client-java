package com.pdfdancer.common.response;

public enum ReadingUnitMode {
    PRIMARY, UNKNOWN;

    public static ReadingUnitMode fromValue(String value) {
        if (value == null) return UNKNOWN;
        try { return valueOf(value); } catch (IllegalArgumentException ignored) { return UNKNOWN; }
    }
}
