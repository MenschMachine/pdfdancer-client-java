package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.PathObjectRef;

public class PathReference extends BaseReference {

    public PathReference(ObjectRef objectRef,
                         PDFDancer client) {
        super(client, objectRef);
    }

    private PathObjectRef ref() {
        return (PathObjectRef) this.objectRef;
    }

    /**
     * Gets the stroke color of this path.
     *
     * @return the stroke color, or null if not set
     */
    public Color getStrokeColor() {
        return ref().getStrokeColor();
    }

    /**
     * Gets the fill color of this path.
     *
     * @return the fill color, or null if not set
     */
    public Color getFillColor() {
        return ref().getFillColor();
    }

    public PathEdit edit() {
        return new PathEdit(client, objectRef);
    }

    public static class PathEdit {

        private final PDFDancer client;
        private final PathObjectRef pathRef;
        private Color strokeColor;
        private Color fillColor;

        public PathEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.pathRef = (PathObjectRef) ref;
        }

        public PathEdit strokeColor(Color color) {
            this.strokeColor = color;
            return this;
        }

        public PathEdit fillColor(Color color) {
            this.fillColor = color;
            return this;
        }

        public boolean apply() {
            Color strokeColorToUse = strokeColor != null ? strokeColor : pathRef.getStrokeColor();
            Color fillColorToUse = fillColor != null ? fillColor : pathRef.getFillColor();
            return client.modifyPath(pathRef, strokeColorToUse, fillColorToUse);
        }
    }
}
