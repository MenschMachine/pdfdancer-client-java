package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;

import java.util.List;

public abstract class BaseReference {
    protected final PDFDancer client;
    protected final ObjectRef objectRef;

    public BaseReference(PDFDancer client, ObjectRef objectRef) {
        this.client = client;
        this.objectRef = objectRef;
    }

    public String getInternalId() {
        return objectRef.getInternalId();
    }

    public Position getPosition() {
        return objectRef.getPosition();
    }

    public boolean delete() {
        return this.client.delete(objectRef);
    }

    public boolean moveTo(double x, double y) {
        return this.client.move(objectRef, new Position(x, y));
    }

    public boolean moveX(int xOffset) {
        Position newPosition = objectRef.getPosition().copy();
        newPosition.moveX(xOffset);
        return this.client.move(objectRef, newPosition);
    }

    public boolean moveY(int yOffset) {
        Position newPosition = objectRef.getPosition().copy();
        newPosition.moveY(yOffset);
        return this.client.move(objectRef, newPosition);
    }

    public ObjectType type() {
        return objectRef.getType();
    }

    /**
     * Redacts this object from the PDF using default replacement text "[REDACTED]".
     *
     * @return true if redaction was successful
     */
    public boolean redact() {
        return redact("[REDACTED]", Color.BLACK);
    }

    /**
     * Redacts this object from the PDF with custom replacement text.
     *
     * @param replacement the replacement text for text content
     * @return true if redaction was successful
     */
    public boolean redact(String replacement) {
        return redact(replacement, Color.BLACK);
    }

    /**
     * Redacts this object from the PDF with custom placeholder color.
     * Useful for images and paths.
     *
     * @param placeholderColor the color for image/path placeholders
     * @return true if redaction was successful
     */
    public boolean redact(Color placeholderColor) {
        return redact("[REDACTED]", placeholderColor);
    }

    /**
     * Redacts this object from the PDF with custom replacement text and placeholder color.
     *
     * @param replacement      the replacement text for text content
     * @param placeholderColor the color for image/path placeholders
     * @return true if redaction was successful
     */
    public boolean redact(String replacement, Color placeholderColor) {
        return client.redact(List.of(this), replacement, placeholderColor).success();
    }
}
