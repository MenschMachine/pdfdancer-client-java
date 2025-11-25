package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageSize;

import java.util.Objects;

public final class AddPageRequest {
    @JsonProperty("pageNumber")
    private final Integer pageNumber;
    @JsonProperty("orientation")
    private final Orientation orientation;
    @JsonProperty("pageSize")
    private final PageSize pageSize;

    @JsonCreator
    public AddPageRequest(@JsonProperty("pageNumber") Integer pageNumber,
                          @JsonProperty("orientation") Orientation orientation,
                          @JsonProperty("pageSize") PageSize pageSize) {
        this.pageNumber = pageNumber;
        this.orientation = orientation;
        this.pageSize = pageSize;
    }

    public Integer pageNumber() {
        return pageNumber;
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
        return Objects.equals(this.pageNumber, that.pageNumber) &&
                Objects.equals(this.orientation, that.orientation) &&
                Objects.equals(this.pageSize, that.pageSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, orientation, pageSize);
    }

    @Override
    public String toString() {
        return "AddPageRequest[" +
                "pageNumber=" + pageNumber + ", " +
                "orientation=" + orientation + ", " +
                "pageSize=" + pageSize + ']';
    }

}
