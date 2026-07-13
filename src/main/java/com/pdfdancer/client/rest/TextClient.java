package com.pdfdancer.client.rest;

import com.pdfdancer.common.request.TextDeleteRequest;
import com.pdfdancer.common.request.TextInsertRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.response.TextEditResponse;

public class TextClient {
    private final PDFDancer root;

    TextClient(PDFDancer root) {
        this.root = root;
    }

    public TextEditResponse replace(TextReplaceRequest request) {
        return root.replaceText(request);
    }

    public TextEditResponse delete(TextDeleteRequest request) {
        return root.deleteText(request);
    }

    public TextEditResponse insert(TextInsertRequest request) {
        return root.insertText(request);
    }

    public TextEditResponse style(TextStyleRequest request) {
        return root.styleText(request);
    }
}
