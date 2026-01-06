package com.pdfdancer.common.model.text;

import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.PDFObject;
import com.pdfdancer.common.model.Position;

/**
 * Represents a single word within a PDF text line.
 * Words are the intermediate text unit between individual characters (TextElement)
 * and full text lines (TextLine), enabling word-level text operations.
 */
public class Word extends PDFObject {
    private String text;

    /**
     * Default constructor for serialization frameworks.
     */
    public Word() {
        super();
    }

    /**
     * Creates a word with specified properties.
     *
     * @param id       unique identifier for the word
     * @param text     the word content
     * @param position location within the PDF document
     */
    public Word(String id, String text, Position position) {
        super(id, position);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.WORD;
    }
}
