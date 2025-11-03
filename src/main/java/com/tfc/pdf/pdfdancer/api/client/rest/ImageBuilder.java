package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.Image;
import com.tfc.pdf.pdfdancer.api.common.model.Position;
import com.tfc.pdf.pdfdancer.api.common.model.PositionBuilder;

import java.io.File;
import java.io.IOException;

public class ImageBuilder {

    private final PDFDancer client;
    private Image image;
    private Position position;

    public ImageBuilder(PDFDancer client) {
        this.client = client;
    }

    public ImageBuilder fromFile(File file) throws IOException {
        this.image = Image.fromFile(file);
        return this;
    }

    public ImageBuilder at(int pageIndex, double x, double y) {
        this.position = new PositionBuilder().onPage(pageIndex).atCoordinates(x, y).build();
        return this;
    }

    public boolean add() {
        return this.client.addImage(this.image, this.position);
    }
}
