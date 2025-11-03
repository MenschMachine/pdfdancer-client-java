package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
/**
 * Request record for deleting PDF objects from a document.
 * This immutable record encapsulates the object reference needed to identify
 * and remove specific PDF objects from a document during editing operations.
 */
public record DeleteRequest(
    /**
     * Reference to the PDF object to be deleted from the document.
     * This reference must identify a valid object within the current session.
     */
    ObjectRef objectRef
) {}
