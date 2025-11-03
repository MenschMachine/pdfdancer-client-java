package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.TextTypeObjectRef;

public class TextLineReference extends BaseReference {
    public TextLineReference(PDFDancer client, TextTypeObjectRef objectRef) {
        super(client, objectRef);
    }

    private TextTypeObjectRef ref() {
        return (TextTypeObjectRef) this.objectRef;
    }

    /**
     * Gets the text content of this line.
     *
     * @return the text content
     */
    public String getText() {
        return (ref()).getText();
    }

    /**
     * Gets the font name used in this line.
     *
     * @return the font name
     */
    public String getFontName() {
        return (ref()).getFontName();
    }

    /**
     * Gets the font size used in this line.
     *
     * @return the font size
     */
    public Double getFontSize() {
        return (ref()).getFontSize();
    }

    /**
     * Gets the text color of this line.
     *
     * @return the color
     */
    public Color getColor() {
        return (ref()).getColor();
    }

    public TextLineEdit edit() {
        return new TextLineEdit(client, objectRef);
    }

    public static class TextLineEdit {

        private final PDFDancer client;
        private final ObjectRef ref;

        public TextLineEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }

        public boolean replace(String newText) {
            return client.modifyTextLine(ref, newText);
        }

    }
}
