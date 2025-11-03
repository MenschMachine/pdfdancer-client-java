package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.PDFObject;
/**
 * Request record for adding new PDF objects to a document.
 * This immutable record encapsulates the data needed to add new content
 * elements to a PDF document, including the complete object specification
 * with getPosition, properties, and content data.
 */
public record AddRequest(PDFObject object) {
}
