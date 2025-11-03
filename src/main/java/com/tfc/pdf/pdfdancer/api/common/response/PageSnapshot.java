package com.tfc.pdf.pdfdancer.api.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfc.pdf.pdfdancer.api.common.model.DocumentFontInfoDto;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
import com.tfc.pdf.pdfdancer.api.common.model.PageRef;

import java.util.List;

/**
 * Represents a complete snapshot of a single PDF page including its metadata and all elements.
 * This response aggregates page information and all contained objects (paragraphs, images, etc.)
 * into a single response, reducing the number of API calls needed to retrieve page content.
 */
public class PageSnapshot {

    private final PageRef pageRef;
    private final List<ObjectRef> elements;
    private final List<DocumentFontInfoDto> fonts;

    @JsonCreator
    public PageSnapshot(@JsonProperty("pageRef") PageRef pageRef,
                        @JsonProperty("elements") List<ObjectRef> elements,
                        @JsonProperty("fonts") List<DocumentFontInfoDto> fonts) {
        this.pageRef = pageRef;
        this.elements = elements;
        this.fonts = fonts;
    }

    public PageRef pageRef() {
        return pageRef;
    }

    public List<ObjectRef> elements() {
        return elements;
    }

    public List<DocumentFontInfoDto> fonts() {
        return fonts;
    }
}
