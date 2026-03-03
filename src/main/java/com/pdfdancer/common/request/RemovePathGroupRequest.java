package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class RemovePathGroupRequest {
    @JsonProperty("pageIndex")
    private final int pageIndex;
    @JsonProperty("groupId")
    private final String groupId;

    @JsonCreator
    public RemovePathGroupRequest(
            @JsonProperty("pageIndex") int pageIndex,
            @JsonProperty("groupId") String groupId) {
        this.pageIndex = pageIndex;
        this.groupId = groupId;
    }

    public int pageIndex() { return pageIndex; }
    public String groupId() { return groupId; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RemovePathGroupRequest) obj;
        return pageIndex == that.pageIndex &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, groupId);
    }

    @Override
    public String toString() {
        return "RemovePathGroupRequest[" +
                "pageIndex=" + pageIndex + ", " +
                "groupId=" + groupId + ']';
    }
}
