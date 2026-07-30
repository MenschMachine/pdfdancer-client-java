package com.pdfdancer.common.response;

import java.util.List;

public record ReadingUnitProvenance(int pageNumber, List<String> sourceElementIds, ReadingUnitBounds bounds) {
    public ReadingUnitProvenance {
        sourceElementIds = sourceElementIds == null ? List.of() : List.copyOf(sourceElementIds);
    }
}
