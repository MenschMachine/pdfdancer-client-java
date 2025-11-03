package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageSize;

import java.util.Objects;

/**
 * Request to create a new blank PDF session.
 */
public final class CreateBlankPdfRequest {
    @JsonProperty("pageSize")
    private final PageSize pageSize;
    @JsonProperty("orientation")
    private final Orientation orientation;
    @JsonProperty("initialPageCount")
    private final Integer initialPageCount;

    /**
     *
     */
    @JsonCreator
    public CreateBlankPdfRequest(
            @JsonProperty("pageSize") PageSize pageSize,           // Page size (A4, LETTER, LEGAL), or null for default (A4)
            @JsonProperty("orientation") Orientation orientation,     // Orientation (PORTRAIT, LANDSCAPE), or null for default (PORTRAIT)
            @JsonProperty("initialPageCount") Integer initialPageCount     // Number of initial blank pages, or null for default (1)
    ) {
        this.pageSize = pageSize;
        this.orientation = orientation;
        this.initialPageCount = initialPageCount;
    }

    public PageSize pageSize() {
        return pageSize != null ? pageSize : PageSize.A4;
    }

    public Orientation orientation() {
        return orientation != null ? orientation : Orientation.PORTRAIT;
    }

    public Integer initialPageCount() {
        return initialPageCount != null ? initialPageCount : 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CreateBlankPdfRequest) obj;
        return Objects.equals(this.pageSize, that.pageSize) &&
                Objects.equals(this.orientation, that.orientation) &&
                Objects.equals(this.initialPageCount, that.initialPageCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, orientation, initialPageCount);
    }

    @Override
    public String toString() {
        return "CreateBlankPdfRequest[" +
                "pageSize=" + pageSize + ", " +
                "orientation=" + orientation + ", " +
                "initialPageCount=" + initialPageCount + ']';
    }

}
