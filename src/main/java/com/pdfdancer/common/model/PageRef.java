package com.pdfdancer.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageRef extends ObjectRef {
    private final PageSize pageSize;
    private final Orientation orientation;

    @JsonCreator
    public PageRef(@JsonProperty("internalId") String internalId,
                   @JsonProperty("position") Position position,
                   @JsonProperty("type") @JsonAlias("objectRefType") ObjectType type,
                   @JsonProperty("objectRefType") ObjectType objectRefType,
                   @JsonProperty("pageSize") PageSize pageSize,
                   @JsonProperty("orientation") Orientation orientation) {
        super(internalId, position, objectRefType, type);
        this.pageSize = pageSize;
        this.orientation = orientation;
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
