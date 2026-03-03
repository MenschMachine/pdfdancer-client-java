package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.BoundingRect;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreatePathGroupRequest {
    @JsonProperty("pageIndex")
    private final int pageIndex;
    @JsonProperty("pathIds")
    private final List<String> pathIds;
    @JsonProperty("region")
    private final BoundingRect region;

    @JsonCreator
    public CreatePathGroupRequest(
            @JsonProperty("pageIndex") int pageIndex,
            @JsonProperty("pathIds") List<String> pathIds,
            @JsonProperty("region") BoundingRect region) {
        if ((pathIds == null || pathIds.isEmpty()) && region == null) {
            throw new IllegalArgumentException("Either pathIds or region must be provided");
        }
        this.pageIndex = pageIndex;
        this.pathIds = pathIds;
        this.region = region;
    }

    public int pageIndex() { return pageIndex; }
    public List<String> pathIds() { return pathIds; }
    public BoundingRect region() { return region; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CreatePathGroupRequest) obj;
        return pageIndex == that.pageIndex &&
                Objects.equals(pathIds, that.pathIds) &&
                Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, pathIds, region);
    }

    @Override
    public String toString() {
        return "CreatePathGroupRequest[" +
                "pageIndex=" + pageIndex + ", " +
                "pathIds=" + pathIds + ", " +
                "region=" + region + ']';
    }
}
