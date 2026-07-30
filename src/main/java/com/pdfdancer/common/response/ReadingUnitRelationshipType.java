package com.pdfdancer.common.response;

public enum ReadingUnitRelationshipType {
    CAPTION_FOR, HEADING_PARENT_OF, UNKNOWN;

    public static ReadingUnitRelationshipType fromValue(String value) {
        if (value == null) return UNKNOWN;
        try { return valueOf(value); } catch (IllegalArgumentException ignored) { return UNKNOWN; }
    }
}
