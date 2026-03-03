package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import com.pdfdancer.common.model.PathGroupInfo;

public class PathGroupReference {
    private final PDFDancer client;
    private final PathGroupInfo info;
    private final int pageIndex;

    public PathGroupReference(PDFDancer client, PathGroupInfo info, int pageIndex) {
        this.client = client;
        this.info = info;
        this.pageIndex = pageIndex;
    }

    public String getGroupId() { return info.getGroupId(); }
    public int getPathCount() { return info.getPathCount(); }
    public BoundingRect getBoundingBox() { return info.getBoundingBox(); }
    public double getX() { return info.getX(); }
    public double getY() { return info.getY(); }
    public int getPageIndex() { return pageIndex; }

    public boolean moveTo(double x, double y) {
        return client.movePathGroup(pageIndex, info.getGroupId(), x, y);
    }

    public boolean scale(double factor) {
        return client.scalePathGroup(pageIndex, info.getGroupId(), factor);
    }

    public boolean rotate(double degrees) {
        return client.rotatePathGroup(pageIndex, info.getGroupId(), degrees);
    }

    public boolean resize(double width, double height) {
        return client.resizePathGroup(pageIndex, info.getGroupId(), width, height);
    }

    public boolean remove() {
        return client.removePathGroup(pageIndex, info.getGroupId());
    }

    @Override
    public String toString() {
        return "PathGroupReference{" +
                "groupId='" + getGroupId() + '\'' +
                ", pathCount=" + getPathCount() +
                ", pageIndex=" + pageIndex +
                '}';
    }
}
