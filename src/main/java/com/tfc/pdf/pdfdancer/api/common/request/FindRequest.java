package com.tfc.pdf.pdfdancer.api.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectType;
import com.tfc.pdf.pdfdancer.api.common.model.Position;

import java.util.Objects;

/**
 * Request record for searching PDF objects within a document.
 * This immutable record encapsulates search criteria for locating PDF objects
 * based on type, getPosition constraints, and optional search hints, enabling
 * flexible and efficient object discovery within PDF documents.
 */
public final class FindRequest {
    @JsonProperty("objectType")
    private final ObjectType objectType;
    @JsonProperty("position")
    private final Position position;
    @JsonProperty("hint")
    private final String hint;

    /**
     *
     */
    @JsonCreator
    public FindRequest(
            /**
             * The type of PDF objects to search for.
             * Null value indicates all object types should be included in results.
             */
            @JsonProperty("objectType") ObjectType objectType,
            /**
             * Positional constraints for the search.
             * Null value indicates no positional filtering should be applied.
             */
            @JsonProperty("position") Position position,
            /**
             * Optional search hint for additional filtering or optimization.
             * The interpretation of this hint depends on the search implementation.
             */
            @JsonProperty("hint") String hint
    ) {
        this.objectType = objectType;
        this.position = position;
        this.hint = hint;
    }

    public ObjectType objectType() {
        return objectType;
    }

    public Position position() {
        return position;
    }

    public String hint() {
        return hint;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FindRequest) obj;
        return Objects.equals(this.objectType, that.objectType) &&
                Objects.equals(this.position, that.position) &&
                Objects.equals(this.hint, that.hint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, position, hint);
    }

    @Override
    public String toString() {
        return "FindRequest[" +
                "objectType=" + objectType + ", " +
                "position=" + position + ", " +
                "hint=" + hint + ']';
    }

}
