package com.pdfdancer.client.rest;

import java.util.Objects;

final class PageSnapshotKey {
    private final int pageIndex;
    private final String typesKey;

    PageSnapshotKey(int pageIndex, String typesKey) {
        this.pageIndex = pageIndex;
        this.typesKey = typesKey;
    }

    public int pageIndex() {
        return pageIndex;
    }

    public String typesKey() {
        return typesKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PageSnapshotKey) obj;
        return this.pageIndex == that.pageIndex &&
                Objects.equals(this.typesKey, that.typesKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, typesKey);
    }

    @Override
    public String toString() {
        return "PageSnapshotKey[" +
                "pageIndex=" + pageIndex + ", " +
                "typesKey=" + typesKey + ']';
    }
}
