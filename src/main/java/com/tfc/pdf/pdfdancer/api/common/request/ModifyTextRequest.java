package com.tfc.pdf.pdfdancer.api.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;

import java.util.Objects;

public final class ModifyTextRequest {
    @JsonProperty("ref")
    private final ObjectRef ref;
    @JsonProperty("newTextLine")
    private final String newTextLine;

    @JsonCreator
    public ModifyTextRequest(
            @JsonProperty("ref") ObjectRef ref,
            @JsonProperty("newTextLine") String newTextLine
    ) {
        this.ref = ref;
        this.newTextLine = newTextLine;
    }

    public ObjectRef ref() {
        return ref;
    }

    public String newTextLine() {
        return newTextLine;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModifyTextRequest) obj;
        return Objects.equals(this.ref, that.ref) &&
                Objects.equals(this.newTextLine, that.newTextLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, newTextLine);
    }

    @Override
    public String toString() {
        return "ModifyTextRequest[" +
                "ref=" + ref + ", " +
                "newTextLine=" + newTextLine + ']';
    }

}
