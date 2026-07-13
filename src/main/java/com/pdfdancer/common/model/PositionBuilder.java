package com.pdfdancer.common.model;

public class PositionBuilder {
    private final Position position = new Position();

    public PositionBuilder onPage(int pageNumber) {
        position.setPageNumber(pageNumber);
        return this;
    }

    public Position build() {
        return position;
    }

    public PositionBuilder atCoordinates(double x, double y) {
        position.atPosition(new Point(x, y));
        return this;
    }
}
