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
    @JsonProperty("fromPageNumber")
    private final int fromPageNumber;
    @JsonProperty("toPageNumber")
    private final int toPageNumber;

    /**
     *
     */
    @JsonCreator
    public PageMoveRequest(
            /**
             * The page number to move (1-based indexing).
             * This page will be extracted from its current getPosition.
             */
            @JsonProperty("fromPageNumber") int fromPageNumber,
            /**
             * The target getPosition for the page (1-based indexing).
             * The specified page will be inserted at this getPosition,
             * with other pages shifting accordingly.
             */
            @JsonProperty("toPageNumber") int toPageNumber
    ) {
        this.fromPageNumber = fromPageNumber;
        this.toPageNumber = toPageNumber;
    }

    public int fromPageNumber() {
        return fromPageNumber;
    }

    public int toPageNumber() {
        return toPageNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PageMoveRequest) obj;
        return this.fromPageNumber == that.fromPageNumber &&
                this.toPageNumber == that.toPageNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromPageNumber, toPageNumber);
    }

    @Override
    public String toString() {
        return "PageMoveRequest[" +
                "fromPageNumber=" + fromPageNumber + ", " +
                "toPageNumber=" + toPageNumber + ']';
    }

}
