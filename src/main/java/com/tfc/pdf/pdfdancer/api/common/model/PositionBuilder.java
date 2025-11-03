package com.tfc.pdf.pdfdancer.api.common.model;

public class PositionBuilder {
    private final Position position = new Position();

    public PositionBuilder onPage(int pageIndex) {
        position.setPageIndex(pageIndex);
        return this;
    }

    public Position build() {
        return position;
    }

    public PositionBuilder textStartsWith(String start) {
        position.setTextStartsWith(start);
        return this;
    }

    public PositionBuilder atCoordinates(double x, double y) {
        position.atPosition(new Point(x, y));
        return this;
    }

    public PositionBuilder textMatches(String pattern) {
        position.setTextPattern(pattern);
        return this;
    }
}
