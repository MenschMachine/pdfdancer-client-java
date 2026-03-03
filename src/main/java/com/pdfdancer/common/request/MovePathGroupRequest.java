package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class MovePathGroupRequest {
    @JsonProperty("pageIndex")
    private final int pageIndex;
    @JsonProperty("groupId")
    private final String groupId;
    @JsonProperty("x")
    private final double x;
    @JsonProperty("y")
    private final double y;

    @JsonCreator
    public MovePathGroupRequest(
            @JsonProperty("pageIndex") int pageIndex,
            @JsonProperty("groupId") String groupId,
            @JsonProperty("x") double x,
            @JsonProperty("y") double y) {
        this.pageIndex = pageIndex;
        this.groupId = groupId;
        this.x = x;
        this.y = y;
    }

    public int pageIndex() { return pageIndex; }
    public String groupId() { return groupId; }
    public double x() { return x; }
    public double y() { return y; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MovePathGroupRequest) obj;
        return pageIndex == that.pageIndex &&
                Objects.equals(groupId, that.groupId) &&
                Double.compare(x, that.x) == 0 &&
                Double.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, groupId, x, y);
    }

    @Override
    public String toString() {
        return "MovePathGroupRequest[" +
                "pageIndex=" + pageIndex + ", " +
                "groupId=" + groupId + ", " +
                "x=" + x + ", " +
                "y=" + y + ']';
    }
}
