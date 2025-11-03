package com.tfc.pdf.pdfdancer.api.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
import com.tfc.pdf.pdfdancer.api.common.model.Position;

import java.util.Objects;

/**
 * Request record for moving PDF objects to new positions within a document.
 * This immutable record encapsulates both the object to be moved and its target
 * getPosition, enabling precise repositioning operations during PDF editing.
 */
public final class MoveRequest {
    @JsonProperty("objectRef")
    private final ObjectRef objectRef;
    @JsonProperty("newPosition")
    private final Position newPosition;

    /**
     *
     */
    @JsonCreator
    public MoveRequest(
            /**
             * Reference to the PDF object to be moved.
             * This reference must identify a valid object within the current session.
             */
            @JsonProperty("objectRef") ObjectRef objectRef,
            /**
             * New getPosition where the object should be moved.
             * This getPosition defines the target coordinates and page location for the object.
             */
            @JsonProperty("newPosition") Position newPosition
    ) {
        this.objectRef = objectRef;
        this.newPosition = newPosition;
    }

    public ObjectRef objectRef() {
        return objectRef;
    }

    public Position newPosition() {
        return newPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MoveRequest) obj;
        return Objects.equals(this.objectRef, that.objectRef) &&
                Objects.equals(this.newPosition, that.newPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectRef, newPosition);
    }

    @Override
    public String toString() {
        return "MoveRequest[" +
                "objectRef=" + objectRef + ", " +
                "newPosition=" + newPosition + ']';
    }

}
