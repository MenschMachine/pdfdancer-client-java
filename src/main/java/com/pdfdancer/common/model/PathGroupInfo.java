package com.pdfdancer.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PathGroupInfo {
    private final String groupId;
    private final int pathCount;
    private final BoundingRect boundingBox;
    private final double x;
    private final double y;

    @JsonCreator
    public PathGroupInfo(@JsonProperty("groupId") String groupId,
                         @JsonProperty("pathCount") int pathCount,
                         @JsonProperty("boundingBox") BoundingRect boundingBox,
                         @JsonProperty("x") double x,
                         @JsonProperty("y") double y) {
        this.groupId = groupId;
        this.pathCount = pathCount;
        this.boundingBox = boundingBox;
        this.x = x;
        this.y = y;
    }

    public String getGroupId() { return groupId; }
    public int getPathCount() { return pathCount; }
    public BoundingRect getBoundingBox() { return boundingBox; }
    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public String toString() {
        return "PathGroupInfo{" +
                "groupId='" + groupId + '\'' +
                ", pathCount=" + pathCount +
                ", boundingBox=" + boundingBox +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PathGroupInfo)) return false;
        PathGroupInfo that = (PathGroupInfo) o;
        return pathCount == that.pathCount &&
                Double.compare(x, that.x) == 0 &&
                Double.compare(y, that.y) == 0 &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(boundingBox, that.boundingBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, pathCount, boundingBox, x, y);
    }
}
