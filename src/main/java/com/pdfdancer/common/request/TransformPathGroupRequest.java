package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TransformPathGroupRequest {
    @JsonProperty("pageIndex")
    private final int pageIndex;
    @JsonProperty("groupId")
    private final String groupId;
    @JsonProperty("transformType")
    private final TransformType transformType;
    @JsonProperty("scaleFactor")
    private final Double scaleFactor;
    @JsonProperty("rotationAngle")
    private final Double rotationAngle;
    @JsonProperty("targetWidth")
    private final Double targetWidth;
    @JsonProperty("targetHeight")
    private final Double targetHeight;

    public enum TransformType {
        SCALE, ROTATE, RESIZE
    }

    @JsonCreator
    public TransformPathGroupRequest(
            @JsonProperty("pageIndex") int pageIndex,
            @JsonProperty("groupId") String groupId,
            @JsonProperty("transformType") TransformType transformType,
            @JsonProperty("scaleFactor") Double scaleFactor,
            @JsonProperty("rotationAngle") Double rotationAngle,
            @JsonProperty("targetWidth") Double targetWidth,
            @JsonProperty("targetHeight") Double targetHeight) {
        this.pageIndex = pageIndex;
        this.groupId = groupId;
        this.transformType = transformType;
        this.scaleFactor = scaleFactor;
        this.rotationAngle = rotationAngle;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    public int pageIndex() { return pageIndex; }
    public String groupId() { return groupId; }
    public TransformType transformType() { return transformType; }
    public Double scaleFactor() { return scaleFactor; }
    public Double rotationAngle() { return rotationAngle; }
    public Double targetWidth() { return targetWidth; }
    public Double targetHeight() { return targetHeight; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TransformPathGroupRequest) obj;
        return pageIndex == that.pageIndex &&
                Objects.equals(groupId, that.groupId) &&
                transformType == that.transformType &&
                Objects.equals(scaleFactor, that.scaleFactor) &&
                Objects.equals(rotationAngle, that.rotationAngle) &&
                Objects.equals(targetWidth, that.targetWidth) &&
                Objects.equals(targetHeight, that.targetHeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, groupId, transformType, scaleFactor, rotationAngle, targetWidth, targetHeight);
    }

    @Override
    public String toString() {
        return "TransformPathGroupRequest[" +
                "pageIndex=" + pageIndex + ", " +
                "groupId=" + groupId + ", " +
                "transformType=" + transformType + ", " +
                "scaleFactor=" + scaleFactor + ", " +
                "rotationAngle=" + rotationAngle + ", " +
                "targetWidth=" + targetWidth + ", " +
                "targetHeight=" + targetHeight + ']';
    }
}
