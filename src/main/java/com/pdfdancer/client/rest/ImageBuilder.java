package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.PositionBuilder;

import java.io.File;
import java.io.IOException;

public class ImageBuilder {

    private final PDFDancer client;
    private final Integer defaultPageNumber;
    private Image image;
    private Position position;

    public ImageBuilder(PDFDancer client) {
        this(client, null);
    }

    public ImageBuilder(PDFDancer client, Integer defaultPageNumber) {
        this.client = client;
        this.defaultPageNumber = defaultPageNumber;
    }

    public ImageBuilder fromFile(File file) throws IOException {
        this.image = Image.fromFile(file);
        return this;
    }

    public ImageBuilder at(int pageNumber, double x, double y) {
        this.position = new PositionBuilder().onPage(pageNumber).atCoordinates(x, y).build();
        return this;
    }

    public ImageBuilder at(double x, double y) {
        if (defaultPageNumber == null) {
            throw new IllegalStateException("Page number is required for a document-scoped image builder");
        }
        return at(defaultPageNumber, x, y);
    }

    public boolean add() {
        if (image == null) throw new IllegalStateException("Call fromFile() before add()");
        if (position == null) throw new IllegalStateException("Call at() before add()");
        return this.client.addImage(this.image, this.position);
    }
}
