package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.ObjectRef;

import java.util.Objects;

public final class ClearClippingRequest {
    @JsonProperty("objectRef")
    private final ObjectRef objectRef;

    @JsonCreator
    public ClearClippingRequest(@JsonProperty("objectRef") ObjectRef objectRef) {
        this.objectRef = objectRef;
    }

    public ObjectRef objectRef() { return objectRef; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClearClippingRequest) obj;
        return Objects.equals(objectRef, that.objectRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectRef);
    }

    @Override
    public String toString() {
        return "ClearClippingRequest[" +
                "objectRef=" + objectRef + ']';
    }
}
