package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.FontRecommendationDto;

import java.util.List;

/**
 * A typed snapshot of the whole document containing pages with elements of type T.
 */
public final class TypedDocumentSnapshot<T extends ObjectRef> {
    private int pageCount;
    private List<FontRecommendationDto> fonts;
    private List<TypedPageSnapshot<T>> pages;

    public TypedDocumentSnapshot() {
    }

    public TypedDocumentSnapshot(int pageCount, List<FontRecommendationDto> fonts, List<TypedPageSnapshot<T>> pages) {
        this.pageCount = pageCount;
        this.fonts = fonts;
        this.pages = pages;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<FontRecommendationDto> getFonts() {
        return fonts;
    }

    public void setFonts(List<FontRecommendationDto> fonts) {
        this.fonts = fonts;
    }

    public List<TypedPageSnapshot<T>> getPages() {
        return pages;
    }

    public void setPages(List<TypedPageSnapshot<T>> pages) {
        this.pages = pages;
    }
}
