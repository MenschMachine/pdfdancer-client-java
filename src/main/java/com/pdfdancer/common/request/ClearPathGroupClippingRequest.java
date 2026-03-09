package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class ClearPathGroupClippingRequest {
    @JsonProperty("pageIndex")
    private final int pageIndex;
    @JsonProperty("pageNumber")
    private final int pageNumber;
    @JsonProperty("groupId")
    private final String groupId;

    public ClearPathGroupClippingRequest(int pageIndex, String groupId) {
        this(Integer.valueOf(pageIndex), null, groupId);
    }

    @JsonCreator
    public ClearPathGroupClippingRequest(
            @JsonProperty("pageIndex") Integer pageIndex,
            @JsonProperty("pageNumber") Integer pageNumber,
            @JsonProperty("groupId") String groupId) {
        this.pageIndex = resolvePageIndex(pageIndex, pageNumber);
        this.pageNumber = this.pageIndex + 1;
        this.groupId = groupId;
    }

    public int pageIndex() { return pageIndex; }
    public int pageNumber() { return pageNumber; }
    public String groupId() { return groupId; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClearPathGroupClippingRequest) obj;
        return pageIndex == that.pageIndex &&
                pageNumber == that.pageNumber &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, pageNumber, groupId);
    }

    @Override
    public String toString() {
        return "ClearPathGroupClippingRequest[" +
                "pageIndex=" + pageIndex + ", " +
                "pageNumber=" + pageNumber + ", " +
                "groupId=" + groupId + ']';
    }

    private static int resolvePageIndex(Integer pageIndex, Integer pageNumber) {
        if (pageIndex != null) {
            return pageIndex;
        }
        if (pageNumber != null) {
            return pageNumber - 1;
        }
        throw new IllegalArgumentException("Either pageIndex or pageNumber must be provided");
    }
}
