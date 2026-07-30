package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record ReadingUnit(
        String id,
        ReadingUnitRole role,
        String rawRole,
        String text,
        Map<ReadingUnitMode, ReadingUnitStreamMembership> stream,
        ReadingUnitProvenance provenance,
        List<ReadingUnitRelationship> relationships) {
    @JsonCreator
    public ReadingUnit(
            @JsonProperty("id") String id,
            @JsonProperty("role") String role,
            @JsonProperty("text") String text,
            @JsonProperty("stream") Map<String, ReadingUnitStreamMembership> stream,
            @JsonProperty("provenance") ReadingUnitProvenance provenance,
            @JsonProperty("relationships") List<ReadingUnitRelationship> relationships) {
        this(id, ReadingUnitRole.fromValue(role), role, text, convertStream(stream), provenance,
                relationships == null ? List.of() : List.copyOf(relationships));
    }

    private static Map<ReadingUnitMode, ReadingUnitStreamMembership> convertStream(
            Map<String, ReadingUnitStreamMembership> source) {
        if (source == null) return Map.of();
        return source.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                entry -> ReadingUnitMode.fromValue(entry.getKey()), Map.Entry::getValue,
                (first, ignored) -> first));
    }
}
