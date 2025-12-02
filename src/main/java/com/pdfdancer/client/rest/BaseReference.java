package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.request.RedactRequest;
import com.pdfdancer.common.response.RedactResponse;

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

    public RedactEdit redact() {
        return new RedactEdit(client, objectRef);
    }

    public static class RedactEdit {
        private final PDFDancer client;
        private final ObjectRef ref;
        private String replacement = "[REDACTED]";
        private Color placeholderColor = Color.BLACK;

        public RedactEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }

        public RedactEdit withReplacement(String replacement) {
            this.replacement = replacement;
            return this;
        }

        public RedactEdit withColor(Color color) {
            this.placeholderColor = color;
            return this;
        }

        public boolean apply() {
            RedactRequest request = RedactRequest.builder()
                    .defaultReplacement(replacement)
                    .placeholderColor(placeholderColor)
                    .addTarget(ref.getType(), ref.getPosition())
                    .build();
            RedactResponse response = client.redact(request);
            return response.success();
        }
    }
}
