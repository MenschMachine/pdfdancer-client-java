package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectType;
import com.tfc.pdf.pdfdancer.api.common.model.Position;
/**
 * Request record for searching PDF objects within a document.
 * This immutable record encapsulates search criteria for locating PDF objects
 * based on type, getPosition constraints, and optional search hints, enabling
 * flexible and efficient object discovery within PDF documents.
 */
public record FindRequest(
    /**
     * The type of PDF objects to search for.
     * Null value indicates all object types should be included in results.
     */
    ObjectType objectType,
    /**
     * Positional constraints for the search.
     * Null value indicates no positional filtering should be applied.
     */
    Position position,
    /**
     * Optional search hint for additional filtering or optimization.
     * The interpretation of this hint depends on the search implementation.
     */
    String hint
) {}
