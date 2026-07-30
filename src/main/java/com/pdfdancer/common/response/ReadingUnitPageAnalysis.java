package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ReadingUnitPageAnalysis(int pageNumber, ReadingUnitMode mode, List<ReadingUnit> units) {
    @JsonCreator
    public ReadingUnitPageAnalysis(@JsonProperty("pageNumber") int pageNumber,
                                   @JsonProperty("mode") String mode,
                                   @JsonProperty("units") List<ReadingUnit> units) {
        this(pageNumber, ReadingUnitMode.fromValue(mode), units == null ? List.of() : List.copyOf(units));
    }
}
