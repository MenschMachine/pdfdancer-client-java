package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
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

    /**
     * Moves this object to absolute page coordinates.
     *
     * @return true if the object was moved
     */
    public boolean moveTo(double x, double y) {
        Position current = objectRef.getPosition();
        Integer pageNumber = current == null ? null : current.getPageNumber();
        Position target = pageNumber == null
                ? new Position(x, y)
                : Position.atPageCoordinates(pageNumber, x, y);
        boolean moved = Boolean.TRUE.equals(this.client.move(objectRef, target));
        if (moved) {
            recordPosition(x, y);
        }
        return moved;
    }

    /**
     * Moves this object horizontally by {@code xOffset} PDF points.
     */
    public boolean moveX(double xOffset) {
        return moveTo(currentX() + xOffset, currentY());
    }

    /**
     * Moves this object vertically by {@code yOffset} PDF points.
     */
    public boolean moveY(double yOffset) {
        return moveTo(currentX(), currentY() + yOffset);
    }

    private double currentX() {
        Position position = objectRef.getPosition();
        Double x = position == null ? null : position.getX();
        if (x == null) {
            throw new ValidationException("Cannot move: object has no position");
        }
        return x;
    }

    private double currentY() {
        Position position = objectRef.getPosition();
        Double y = position == null ? null : position.getY();
        if (y == null) {
            throw new ValidationException("Cannot move: object has no position");
        }
        return y;
    }

    /**
     * Keeps the local position in sync after a move so relative moves compose.
     * Installs an updated copy instead of mutating the existing position, so a
     * {@link Position} the caller already holds is left untouched.
     */
    private void recordPosition(double x, double y) {
        Position position = objectRef.getPosition();
        if (position == null) {
            return;
        }
        BoundingRect rect = position.getBoundingRect();
        Position updated = position.copy();
        updated.setBoundingRect(new BoundingRect(x, y,
                rect == null ? 0 : rect.getWidth(),
                rect == null ? 0 : rect.getHeight()));
        objectRef.setPosition(updated);
    }

    public boolean clearClipping() {
        return client.clearClipping(objectRef);
    }

    public ObjectType type() {
        return objectRef.getType();
    }
}
