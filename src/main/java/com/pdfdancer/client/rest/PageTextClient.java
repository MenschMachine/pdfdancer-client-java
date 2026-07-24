package com.pdfdancer.client.rest;

import com.pdfdancer.common.request.TextDeleteRequest;
import com.pdfdancer.common.request.TextInsertRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.response.TextEditResponse;

import java.util.List;

public class PageTextClient {
    private final PDFDancer root;
    private final int pageNumber;

    PageTextClient(PDFDancer root, int pageNumber) {
        this.root = root;
        this.pageNumber = pageNumber;
    }

    public TextEditResponse replace(TextReplaceRequest request) {
        return root.replaceText(request.withPages(List.of(pageNumber)));
    }

    public TextEditResponse delete(TextDeleteRequest request) {
        return root.deleteText(request.withPages(List.of(pageNumber)));
    }

    public TextEditResponse insert(TextInsertRequest request) {
        return root.insertText(request.withPages(List.of(pageNumber)));
    }

    public TextEditResponse style(TextStyleRequest request) {
        return root.styleText(request.withPages(List.of(pageNumber)));
    }
}
