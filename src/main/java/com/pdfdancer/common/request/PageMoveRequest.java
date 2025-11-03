package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Request record for reordering pages within a PDF document.
 * This immutable record specifies a page movement operation, defining
 * which page should be moved and where it should be repositioned
 * in the document's page sequence.
 */
public final class PageMoveRequest {
    @JsonProperty("fromPageIndex")
    private final int fromPageIndex;
    @JsonProperty("toPageIndex")
    private final int toPageIndex;

    /**
     *
     */
    @JsonCreator
    public PageMoveRequest(
            /**
             * The page number to move (1-based indexing).
             * This page will be extracted from its current getPosition.
             */
            @JsonProperty("fromPageIndex") int fromPageIndex,
            /**
             * The target getPosition for the page (1-based indexing).
             * The specified page will be inserted at this getPosition,
             * with other pages shifting accordingly.
             */
            @JsonProperty("toPageIndex") int toPageIndex
    ) {
        this.fromPageIndex = fromPageIndex;
        this.toPageIndex = toPageIndex;
    }

    public int fromPageIndex() {
        return fromPageIndex;
    }

    public int toPageIndex() {
        return toPageIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PageMoveRequest) obj;
        return this.fromPageIndex == that.fromPageIndex &&
                this.toPageIndex == that.toPageIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromPageIndex, toPageIndex);
    }

    @Override
    public String toString() {
        return "PageMoveRequest[" +
                "fromPageIndex=" + fromPageIndex + ", " +
                "toPageIndex=" + toPageIndex + ']';
    }

}
