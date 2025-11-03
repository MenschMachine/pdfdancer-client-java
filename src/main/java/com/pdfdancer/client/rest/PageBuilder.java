package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageRef;
import com.pdfdancer.common.model.PageSize;
import com.pdfdancer.common.request.AddPageRequest;

public class PageBuilder {

    private final PDFDancer client;
    private Integer pageIndex;
    private Orientation orientation;
    private PageSize pageSize;

    public PageBuilder(PDFDancer client) {
        this.client = client;
    }

    public PageBuilder atIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        return this;
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
        return new AddPageRequest(pageIndex, orientation, pageSize);
    }
}
