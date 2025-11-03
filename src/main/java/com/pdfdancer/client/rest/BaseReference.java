package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;

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
}
