package com.tfc.pdf.pdfdancer.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class DocumentFontInfoDto {
    @JsonProperty("documentFontName")
    private final String documentFontName;
    @JsonProperty("systemFontName")
    private final String systemFontName;

    @JsonCreator
    public DocumentFontInfoDto(@JsonProperty("documentFontName") String documentFontName,
                               @JsonProperty("systemFontName") String systemFontName) {
        this.documentFontName = documentFontName;
        this.systemFontName = systemFontName;
    }

    public String documentFontName() {
        return documentFontName;
    }

    public String systemFontName() {
        return systemFontName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DocumentFontInfoDto) obj;
        return Objects.equals(this.documentFontName, that.documentFontName) &&
                Objects.equals(this.systemFontName, that.systemFontName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentFontName, systemFontName);
    }

    @Override
    public String toString() {
        return "DocumentFontInfoDto[" +
                "documentFontName=" + documentFontName + ", " +
                "systemFontName=" + systemFontName + ']';
    }

}
