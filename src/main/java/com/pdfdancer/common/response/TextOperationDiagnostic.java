package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextOperationDiagnostic {
    @JsonProperty("page")
    private final Integer page;
    @JsonProperty("code")
    private final String code;
    @JsonProperty("message")
    private final String message;
    @JsonProperty("elementIds")
    private final List<String> elementIds;
    @JsonProperty("reflowUnitIds")
    private final List<String> reflowUnitIds;

    @JsonCreator
    public TextOperationDiagnostic(@JsonProperty("page") Integer page,
                                   @JsonProperty("code") String code,
                                   @JsonProperty("message") String message,
                                   @JsonProperty("elementIds") List<String> elementIds,
                                   @JsonProperty("reflowUnitIds") List<String> reflowUnitIds) {
        this.page = page;
        this.code = code;
        this.message = message;
        this.elementIds = elementIds == null ? null : List.copyOf(elementIds);
        this.reflowUnitIds = reflowUnitIds == null ? null : List.copyOf(reflowUnitIds);
    }

    public Integer page() { return page; }
    public String code() { return code; }
    public String message() { return message; }
    public List<String> elementIds() { return elementIds; }
    public List<String> reflowUnitIds() { return reflowUnitIds; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextOperationDiagnostic) obj;
        return Objects.equals(page, that.page) &&
                Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(elementIds, that.elementIds) &&
                Objects.equals(reflowUnitIds, that.reflowUnitIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, code, message, elementIds, reflowUnitIds);
    }
}
