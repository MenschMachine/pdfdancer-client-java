package com.tfc.pdf.pdfdancer.api.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.DocumentFontInfoDto;

import java.util.List;

/**
 * Represents a complete snapshot of a PDF document including metadata and all pages.
 */
public class DocumentSnapshot {

    private final int pageCount;
    private final List<DocumentFontInfoDto> fonts;
    private final List<PageSnapshot> pages;

    @JsonCreator
    public DocumentSnapshot(@JsonProperty("pageCount") int pageCount,
                            @JsonProperty("fonts") List<DocumentFontInfoDto> fonts,
                            @JsonProperty("pages") List<PageSnapshot> pages) {
        this.pageCount = pageCount;
        this.fonts = fonts;
        this.pages = pages;
    }

    public int pageCount() {
        return pageCount;
    }

    public List<DocumentFontInfoDto> fonts() {
        return fonts;
    }

    public List<PageSnapshot> pages() {
        return pages;
    }
}
