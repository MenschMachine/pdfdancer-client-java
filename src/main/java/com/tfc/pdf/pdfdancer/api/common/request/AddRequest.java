package com.tfc.pdf.pdfdancer.api.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.PDFObject;

import java.util.Objects;

/**
 * Request record for adding new PDF objects to a document.
 * This immutable record encapsulates the data needed to add new content
 * elements to a PDF document, including the complete object specification
 * with getPosition, properties, and content data.
 */
public final class AddRequest {
    @JsonProperty("object")
    private final PDFObject object;

    /**
     *
     */
    @JsonCreator
    public AddRequest(@JsonProperty("object") PDFObject object) {
        this.object = object;
    }

    public PDFObject object() {
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AddRequest) obj;
        return Objects.equals(this.object, that.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }

    @Override
    public String toString() {
        return "AddRequest[" +
                "object=" + object + ']';
    }

}
