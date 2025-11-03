package com.tfc.pdf.pdfdancer.api.common.request;
/**
 * Request record for reordering pages within a PDF document.
 * This immutable record specifies a page movement operation, defining
 * which page should be moved and where it should be repositioned
 * in the document's page sequence.
 */
public record PageMoveRequest(
    /**
     * The page number to move (1-based indexing).
     * This page will be extracted from its current getPosition.
     */
    int fromPageIndex,
    /**
     * The target getPosition for the page (1-based indexing).
     * The specified page will be inserted at this getPosition,
     * with other pages shifting accordingly.
     */
    int toPageIndex
) {}
