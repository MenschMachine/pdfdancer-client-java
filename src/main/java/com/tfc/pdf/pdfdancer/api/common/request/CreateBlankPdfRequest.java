package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.Orientation;
import com.tfc.pdf.pdfdancer.api.common.model.PageSize;
/**
 * Request to create a new blank PDF session.
 */
public record CreateBlankPdfRequest(
        PageSize pageSize,           // Page size (A4, LETTER, LEGAL), or null for default (A4)
        Orientation orientation,     // Orientation (PORTRAIT, LANDSCAPE), or null for default (PORTRAIT)
        Integer initialPageCount     // Number of initial blank pages, or null for default (1)
) {
    public PageSize pageSize() {
        return pageSize != null ? pageSize : PageSize.A4;
    }
    public Orientation orientation() {
        return orientation != null ? orientation : Orientation.PORTRAIT;
    }
    public Integer initialPageCount() {
        return initialPageCount != null ? initialPageCount : 1;
    }
}
