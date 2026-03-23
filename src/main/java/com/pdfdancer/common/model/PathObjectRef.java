package com.pdfdancer.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lightweight reference to a PDF path object providing identity, type, and styling information.
 * Object references enable efficient API operations by providing a way to identify
 * and reference PDF objects without transferring their complete content.
 * This design pattern reduces payload sizes and improves performance for
 * operations that only need object identification and basic properties.
 */
public class PathObjectRef extends ObjectRef {
    private final Color strokeColor;
    private final Color fillColor;
    private final Double strokeWidth;
    private final double[] dashArray;
    private final Double dashPhase;

    @JsonCreator
    public PathObjectRef(@JsonProperty("internalId") String internalId,
                         @JsonProperty("position") Position position,
                         @JsonProperty("type") @JsonAlias("objectRefType") ObjectType objectType,
                         @JsonProperty("objectRefType") ObjectType objectRefType,
                         @JsonProperty("strokeColor") Color strokeColor,
                         @JsonProperty("fillColor") Color fillColor,
                         @JsonProperty("strokeWidth") Double strokeWidth,
                         @JsonProperty("dashArray") double[] dashArray,
                         @JsonProperty("dashPhase") Double dashPhase) {
        super(internalId, position, objectRefType, objectType);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.strokeWidth = strokeWidth;
        this.dashArray = dashArray;
        this.dashPhase = dashPhase;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Double getStrokeWidth() {
        return strokeWidth;
    }

    public double[] getDashArray() {
        return dashArray;
    }

    public Double getDashPhase() {
        return dashPhase;
    }
}