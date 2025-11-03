package com.tfc.pdf.pdfdancer.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class FontRecommendationDto {

    private final String fontName;
    private final FontType fontType;
    private final double similarityScore;

    @JsonCreator
    public FontRecommendationDto(
            @JsonProperty("fontName") String fontName,
            @JsonProperty("fontType") FontType fontType,
            @JsonProperty("similarityScore") double similarityScore) {
        this.fontName = fontName;
        this.fontType = fontType;
        this.similarityScore = similarityScore;
    }

    public String fontName() {
        return fontName;
    }

    public FontType fontType() {
        return fontType;
    }

    public double similarityScore() {
        return similarityScore;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FontRecommendationDto) obj;
        return Objects.equals(this.fontName, that.fontName) &&
                Objects.equals(this.fontType, that.fontType) &&
                Double.doubleToLongBits(this.similarityScore) == Double.doubleToLongBits(that.similarityScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontName, fontType, similarityScore);
    }

    @Override
    public String toString() {
        return "FontRecommendationDto[" +
                "fontName=" + fontName + ", " +
                "fontType=" + fontType + ", " +
                "similarityScore=" + similarityScore + ']';
    }

}
