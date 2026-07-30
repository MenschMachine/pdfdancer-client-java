package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ReadingUnitDocumentAnalysis(int pageCount, ReadingUnitMode mode, List<ReadingUnitPageAnalysis> pages) {
    @JsonCreator
    public ReadingUnitDocumentAnalysis(@JsonProperty("pageCount") int pageCount,
                                       @JsonProperty("mode") String mode,
                                       @JsonProperty("pages") List<ReadingUnitPageAnalysis> pages) {
        this(pageCount, ReadingUnitMode.fromValue(mode), pages == null ? List.of() : List.copyOf(pages));
    }
}
