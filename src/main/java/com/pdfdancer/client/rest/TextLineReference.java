package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.TextObjectRef;

/**
 * Reference to a single line of text in a PDF document.
 * <p>
 * Lines can be moved ({@link #moveTo(double, double)}, {@link #moveX(double)},
 * {@link #moveY(double)}) and deleted. Editing the text itself goes through
 * {@code PDFDancer.text()}.
 */
public class TextLineReference extends BaseReference {

    public TextLineReference(PDFDancer client, ObjectRef objectRef) {
        super(client, objectRef);
    }

    /**
     * Returns the text of this line.
     *
     * @return the text, or null if the server did not provide it
     */
    public String getText() {
        return objectRef instanceof TextObjectRef textRef ? textRef.getText() : null;
    }

    @Override
    public String toString() {
        return "TextLineReference{id=" + getInternalId() + ", text=" + getText() + "}";
    }
}
