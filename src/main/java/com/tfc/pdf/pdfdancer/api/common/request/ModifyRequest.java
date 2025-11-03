package com.tfc.pdf.pdfdancer.api.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
import com.tfc.pdf.pdfdancer.api.common.model.PDFObject;

import java.util.Objects;

public final class ModifyRequest {
    @JsonProperty("ref")
    private final ObjectRef ref;
    @JsonProperty("newObject")
    private final PDFObject newObject;

    @JsonCreator
    public ModifyRequest(
            @JsonProperty("ref") ObjectRef ref,
            @JsonProperty("newObject") PDFObject newObject
    ) {
        this.ref = ref;
        this.newObject = newObject;
    }

    public ObjectRef ref() {
        return ref;
    }

    public PDFObject newObject() {
        return newObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModifyRequest) obj;
        return Objects.equals(this.ref, that.ref) &&
                Objects.equals(this.newObject, that.newObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, newObject);
    }

    @Override
    public String toString() {
        return "ModifyRequest[" +
                "ref=" + ref + ", " +
                "newObject=" + newObject + ']';
    }

}
