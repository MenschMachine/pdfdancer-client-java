package com.pdfdancer.client.rest;

import java.util.Objects;

final class PageSnapshotKey {
    private final int pageNumber;
    private final String typesKey;

    PageSnapshotKey(int pageNumber, String typesKey) {
        this.pageNumber = pageNumber;
        this.typesKey = typesKey;
    }

    public int pageNumber() {
        return pageNumber;
    }

    public String typesKey() {
        return typesKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PageSnapshotKey) obj;
        return this.pageNumber == that.pageNumber &&
                Objects.equals(this.typesKey, that.typesKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, typesKey);
    }

    @Override
    public String toString() {
        return "PageSnapshotKey[" +
                "pageNumber=" + pageNumber + ", " +
                "typesKey=" + typesKey + ']';
    }
}
