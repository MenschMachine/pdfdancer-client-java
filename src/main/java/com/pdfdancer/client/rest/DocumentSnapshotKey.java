package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;

import java.util.Objects;

final class DocumentSnapshotKey {
    private final Class<? extends ObjectRef> elementClass;
    private final String typesKey;

    DocumentSnapshotKey(Class<? extends ObjectRef> elementClass, String typesKey) {
        this.elementClass = elementClass;
        this.typesKey = typesKey;
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
        var that = (DocumentSnapshotKey) obj;
        return Objects.equals(this.elementClass, that.elementClass) &&
                Objects.equals(this.typesKey, that.typesKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementClass, typesKey);
    }

    @Override
    public String toString() {
        return "DocumentSnapshotKey[" +
                "elementClass=" + elementClass + ", " +
                "typesKey=" + typesKey + ']';
    }
}
