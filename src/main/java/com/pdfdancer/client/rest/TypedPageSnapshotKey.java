package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;

import java.util.Objects;

final class TypedPageSnapshotKey {
    private final int pageIndex;
    private final Class<? extends ObjectRef> elementClass;
    private final String typesKey;

    TypedPageSnapshotKey(int pageIndex, Class<? extends ObjectRef> elementClass, String typesKey) {
        this.pageIndex = pageIndex;
        this.elementClass = elementClass;
        this.typesKey = typesKey;
    }

    public int pageIndex() {
        return pageIndex;
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
        return this.pageIndex == that.pageIndex &&
                Objects.equals(this.elementClass, that.elementClass) &&
                Objects.equals(this.typesKey, that.typesKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, elementClass, typesKey);
    }

    @Override
    public String toString() {
        return "TypedPageSnapshotKey[" +
                "pageIndex=" + pageIndex + ", " +
                "elementClass=" + elementClass + ", " +
                "typesKey=" + typesKey + ']';
    }
}
