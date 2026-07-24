package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import com.pdfdancer.common.model.PathGroupInfo;

public class PathGroupReference {
    private final PDFDancer client;
    private final PathGroupInfo info;
    private final int pageNumber;

    public PathGroupReference(PDFDancer client, PathGroupInfo info, int pageNumber) {
        this.client = client;
        this.info = info;
        this.pageNumber = pageNumber;
    }

    public String getGroupId() { return info.getGroupId(); }
    public int getPathCount() { return info.getPathCount(); }
    public BoundingRect getBoundingBox() { return info.getBoundingBox(); }
    public double getX() { return info.getX(); }
    public double getY() { return info.getY(); }
    public int getPageNumber() { return pageNumber; }

    public boolean moveTo(double x, double y) {
        return client.movePathGroup(pageNumber, info.getGroupId(), x, y);
    }

    public boolean scale(double factor) {
        return client.scalePathGroup(pageNumber, info.getGroupId(), factor);
    }

    public boolean rotate(double degrees) {
        return client.rotatePathGroup(pageNumber, info.getGroupId(), degrees);
    }

    public boolean resize(double width, double height) {
        return client.resizePathGroup(pageNumber, info.getGroupId(), width, height);
    }

    public boolean remove() {
        return client.removePathGroup(pageNumber, info.getGroupId());
    }

    public boolean clearClipping() {
        return client.clearPathGroupClipping(pageNumber, info.getGroupId());
    }

    @Override
    public String toString() {
        return "PathGroupReference{" +
                "groupId='" + getGroupId() + '\'' +
                ", pathCount=" + getPathCount() +
                ", pageNumber=" + pageNumber +
                '}';
    }
}
