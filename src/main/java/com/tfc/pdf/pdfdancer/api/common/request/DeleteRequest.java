package com.tfc.pdf.pdfdancer.api.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;

import java.util.Objects;

/**
 * Request record for deleting PDF objects from a document.
 * This immutable record encapsulates the object reference needed to identify
 * and remove specific PDF objects from a document during editing operations.
 */
public final class DeleteRequest {
    @JsonProperty("objectRef")
    private final ObjectRef objectRef;

    /**
     *
     */
    @JsonCreator
    public DeleteRequest(
            /**
             * Reference to the PDF object to be deleted from the document.
             * This reference must identify a valid object within the current session.
             */
            @JsonProperty("objectRef") ObjectRef objectRef
    ) {
        this.objectRef = objectRef;
    }

    public ObjectRef objectRef() {
        return objectRef;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DeleteRequest) obj;
        return Objects.equals(this.objectRef, that.objectRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectRef);
    }

    @Override
    public String toString() {
        return "DeleteRequest[" +
                "objectRef=" + objectRef + ']';
    }

}
