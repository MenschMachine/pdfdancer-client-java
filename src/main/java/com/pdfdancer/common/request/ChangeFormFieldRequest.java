package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.ObjectRef;

import java.util.Objects;

public final class ChangeFormFieldRequest {
    @JsonProperty("ref")
    private final ObjectRef ref;
    @JsonProperty("value")
    private final String value;

    @JsonCreator
    public ChangeFormFieldRequest(@JsonProperty("ref") ObjectRef ref,
                                  @JsonProperty("value") String value) {
        this.ref = ref;
        this.value = value;
    }

    public ObjectRef ref() {
        return ref;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChangeFormFieldRequest) obj;
        return Objects.equals(this.ref, that.ref) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, value);
    }

    @Override
    public String toString() {
        return "ChangeFormFieldRequest[" +
                "ref=" + ref + ", " +
                "value=" + value + ']';
    }

}
