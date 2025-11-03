package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
import com.tfc.pdf.pdfdancer.api.common.model.Position;
/**
 * Request record for moving PDF objects to new positions within a document.
 * This immutable record encapsulates both the object to be moved and its target
 * getPosition, enabling precise repositioning operations during PDF editing.
 */
public record MoveRequest(
    /**
     * Reference to the PDF object to be moved.
     * This reference must identify a valid object within the current session.
     */
    ObjectRef objectRef,
    /**
     * New getPosition where the object should be moved.
     * This getPosition defines the target coordinates and page location for the object.
     */
    Position newPosition
) {}
