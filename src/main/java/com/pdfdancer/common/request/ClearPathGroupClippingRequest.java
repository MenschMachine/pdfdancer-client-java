package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class ClearPathGroupClippingRequest {
    @JsonProperty("pageNumber")
    private final int pageNumber;
    @JsonProperty("groupId")
    private final String groupId;

    @JsonCreator
    public ClearPathGroupClippingRequest(
            @JsonProperty("pageNumber") int pageNumber,
            @JsonProperty("groupId") String groupId) {
        this.pageNumber = pageNumber;
        this.groupId = groupId;
    }

    public int pageNumber() { return pageNumber; }
    public String groupId() { return groupId; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClearPathGroupClippingRequest) obj;
        return pageNumber == that.pageNumber &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, groupId);
    }

    @Override
    public String toString() {
        return "ClearPathGroupClippingRequest[" +
                "pageNumber=" + pageNumber + ", " +
                "groupId=" + groupId + ']';
    }
}
