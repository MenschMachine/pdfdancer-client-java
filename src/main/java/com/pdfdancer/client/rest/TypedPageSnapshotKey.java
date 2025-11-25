package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;

import java.util.Objects;

final class TypedPageSnapshotKey {
    private final int pageNumber;
    private final Class<? extends ObjectRef> elementClass;
    private final String typesKey;

    TypedPageSnapshotKey(int pageNumber, Class<? extends ObjectRef> elementClass, String typesKey) {
        this.pageNumber = pageNumber;
        this.elementClass = elementClass;
        this.typesKey = typesKey;
    }

    public int pageNumber() {
        return pageNumber;
    }

    public Class<? extends ObjectRef> elementClass() {
        return elementClass;
    }

    public String typesKey() {
        return typesKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TypedPageSnapshotKey) obj;
        return this.pageNumber == that.pageNumber &&
                Objects.equals(this.elementClass, that.elementClass) &&
                Objects.equals(this.typesKey, that.typesKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, elementClass, typesKey);
    }

    @Override
    public String toString() {
        return "TypedPageSnapshotKey[" +
                "pageNumber=" + pageNumber + ", " +
                "elementClass=" + elementClass + ", " +
                "typesKey=" + typesKey + ']';
    }
}
