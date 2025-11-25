package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageRef;
import com.pdfdancer.common.model.PageSize;
import com.pdfdancer.common.request.AddPageRequest;

public class PageBuilder {

    private final PDFDancer client;
    private Integer pageNumber;
    private Orientation orientation;
    private PageSize pageSize;

    public PageBuilder(PDFDancer client) {
        this.client = client;
    }

    /**
     * Sets the page number where the new page should be inserted (1-based).
     * Page 1 is the first page.
     *
     * @param pageNumber the 1-based page number (must be >= 1)
     * @return this builder
     * @throws IllegalArgumentException if pageNumber is less than 1
     */
    public PageBuilder atPage(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1 (1-based indexing)");
        }
        this.pageNumber = pageNumber;
        return this;
    }

    /**
     * @deprecated Use {@link #atPage(int)} instead. This method will be removed in a future release.
     */
    @Deprecated
    public PageBuilder atIndex(int pageNumber) {
        return atPage(pageNumber + 1);
    }

    public PageBuilder orientation(Orientation orientation) {
        this.orientation = orientation;
        return this;
    }

    public PageBuilder portrait() {
        this.orientation = Orientation.PORTRAIT;
        return this;
    }

    public PageBuilder landscape() {
        this.orientation = Orientation.LANDSCAPE;
        return this;
    }

    public PageBuilder pageSize(PageSize pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public PageBuilder a4() {
        this.pageSize = PageSize.A4;
        return this;
    }

    public PageBuilder letter() {
        this.pageSize = PageSize.LETTER;
        return this;
    }

    public PageBuilder a3() {
        this.pageSize = PageSize.A3;
        return this;
    }

    public PageBuilder a5() {
        this.pageSize = PageSize.A5;
        return this;
    }

    public PageBuilder legal() {
        this.pageSize = PageSize.LEGAL;
        return this;
    }

    public PageBuilder customSize(double width, double height) {
        this.pageSize = PageSize.custom(width, height);
        return this;
    }

    public PageRef add() {
        return client.addPage(buildRequest());
    }

    protected AddPageRequest buildRequest() {
        return new AddPageRequest(pageNumber, orientation, pageSize);
    }
}
