package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ReadingUnitRelationship(
        ReadingUnitRelationshipType type,
        String rawType,
        String targetUnitId) {
    @JsonCreator
    public ReadingUnitRelationship(@JsonProperty("type") String type,
                                   @JsonProperty("targetUnitId") String targetUnitId) {
        this(ReadingUnitRelationshipType.fromValue(type), type, targetUnitId);
    }
}
