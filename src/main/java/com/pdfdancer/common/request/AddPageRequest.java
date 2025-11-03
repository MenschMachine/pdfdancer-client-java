package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageSize;

import java.util.Objects;

public final class AddPageRequest {
    @JsonProperty("pageIndex")
    private final Integer pageIndex;
    @JsonProperty("orientation")
    private final Orientation orientation;
    @JsonProperty("pageSize")
    private final PageSize pageSize;

    @JsonCreator
    public AddPageRequest(@JsonProperty("pageIndex") Integer pageIndex,
                          @JsonProperty("orientation") Orientation orientation,
                          @JsonProperty("pageSize") PageSize pageSize) {
        this.pageIndex = pageIndex;
        this.orientation = orientation;
        this.pageSize = pageSize;
    }

    public Integer pageIndex() {
        return pageIndex;
    }

    public Orientation orientation() {
        return orientation;
    }

    public PageSize pageSize() {
        return pageSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AddPageRequest) obj;
        return Objects.equals(this.pageIndex, that.pageIndex) &&
                Objects.equals(this.orientation, that.orientation) &&
                Objects.equals(this.pageSize, that.pageSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, orientation, pageSize);
    }

    @Override
    public String toString() {
        return "AddPageRequest[" +
                "pageIndex=" + pageIndex + ", " +
                "orientation=" + orientation + ", " +
                "pageSize=" + pageSize + ']';
    }

}
