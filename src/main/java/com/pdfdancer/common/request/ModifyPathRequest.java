package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;

import java.util.Objects;

public final class ModifyPathRequest {
    @JsonProperty("ref")
    private final ObjectRef ref;
    @JsonProperty("strokeColor")
    private final Color strokeColor;
    @JsonProperty("fillColor")
    private final Color fillColor;

    @JsonCreator
    public ModifyPathRequest(@JsonProperty("ref") ObjectRef ref,
                             @JsonProperty("strokeColor") Color strokeColor,
                             @JsonProperty("fillColor") Color fillColor) {
        this.ref = ref;
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    public ObjectRef ref() {
        return ref;
    }

    public Color strokeColor() {
        return strokeColor;
    }

    public Color fillColor() {
        return fillColor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ModifyPathRequest that = (ModifyPathRequest) obj;
        return Objects.equals(this.ref, that.ref) &&
                Objects.equals(this.strokeColor, that.strokeColor) &&
                Objects.equals(this.fillColor, that.fillColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, strokeColor, fillColor);
    }

    @Override
    public String toString() {
        return "ModifyPathRequest[" +
                "ref=" + ref + ", " +
                "strokeColor=" + strokeColor + ", " +
                "fillColor=" + fillColor + ']';
    }
}