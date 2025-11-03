package com.tfc.pdf.pdfdancer.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class ErrorResponse {
    @JsonProperty("error")
    private final String error;
    @JsonProperty("message")
    private final String message;

    @JsonCreator
    public ErrorResponse(@JsonProperty("error") String error,
                         @JsonProperty("message") String message) {
        this.error = error;
        this.message = message;
    }

    public String error() {
        return error;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ErrorResponse) obj;
        return Objects.equals(this.error, that.error) &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, message);
    }

    @Override
    public String toString() {
        return "ErrorResponse[" +
                "error=" + error + ", " +
                "message=" + message + ']';
    }

}
