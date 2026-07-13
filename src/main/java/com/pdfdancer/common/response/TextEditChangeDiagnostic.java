package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextEditChangeDiagnostic {
    @JsonProperty("page")
    private final Integer page;
    @JsonProperty("operation")
    private final String operation;
    @JsonProperty("sourceText")
    private final String sourceText;
    @JsonProperty("resultText")
    private final String resultText;
    @JsonProperty("requestedLayoutMode")
    private final String requestedLayoutMode;
    @JsonProperty("requestedLayoutProfile")
    private final String requestedLayoutProfile;
    @JsonProperty("appliedLayoutMode")
    private final String appliedLayoutMode;
    @JsonProperty("elementIds")
    private final List<String> elementIds;
    @JsonProperty("generatedElementIds")
    private final List<String> generatedElementIds;
    @JsonProperty("reflowUnitIds")
    private final List<String> reflowUnitIds;

    @JsonCreator
    public TextEditChangeDiagnostic(@JsonProperty("page") Integer page,
                                    @JsonProperty("operation") String operation,
                                    @JsonProperty("sourceText") String sourceText,
                                    @JsonProperty("resultText") String resultText,
                                    @JsonProperty("requestedLayoutMode") String requestedLayoutMode,
                                    @JsonProperty("requestedLayoutProfile") String requestedLayoutProfile,
                                    @JsonProperty("appliedLayoutMode") String appliedLayoutMode,
                                    @JsonProperty("elementIds") List<String> elementIds,
                                    @JsonProperty("generatedElementIds") List<String> generatedElementIds,
                                    @JsonProperty("reflowUnitIds") List<String> reflowUnitIds) {
        this.page = page;
        this.operation = operation;
        this.sourceText = sourceText;
        this.resultText = resultText;
        this.requestedLayoutMode = requestedLayoutMode;
        this.requestedLayoutProfile = requestedLayoutProfile;
        this.appliedLayoutMode = appliedLayoutMode;
        this.elementIds = elementIds == null ? null : List.copyOf(elementIds);
        this.generatedElementIds = generatedElementIds == null ? null : List.copyOf(generatedElementIds);
        this.reflowUnitIds = reflowUnitIds == null ? null : List.copyOf(reflowUnitIds);
    }

    public TextEditChangeDiagnostic(Integer page,
                                    String operation,
                                    String sourceText,
                                    String resultText,
                                    String requestedLayoutMode,
                                    String requestedLayoutProfile,
                                    String appliedLayoutMode,
                                    List<String> elementIds,
                                    List<String> reflowUnitIds) {
        this(page, operation, sourceText, resultText, requestedLayoutMode, requestedLayoutProfile,
                appliedLayoutMode, elementIds, null, reflowUnitIds);
    }

    public Integer page() { return page; }
    public String operation() { return operation; }
    public String sourceText() { return sourceText; }
    public String resultText() { return resultText; }
    public String requestedLayoutMode() { return requestedLayoutMode; }
    public String requestedLayoutProfile() { return requestedLayoutProfile; }
    public String appliedLayoutMode() { return appliedLayoutMode; }
    public List<String> elementIds() { return elementIds; }
    public List<String> generatedElementIds() { return generatedElementIds; }
    public List<String> reflowUnitIds() { return reflowUnitIds; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextEditChangeDiagnostic) obj;
        return Objects.equals(page, that.page) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(sourceText, that.sourceText) &&
                Objects.equals(resultText, that.resultText) &&
                Objects.equals(requestedLayoutMode, that.requestedLayoutMode) &&
                Objects.equals(requestedLayoutProfile, that.requestedLayoutProfile) &&
                Objects.equals(appliedLayoutMode, that.appliedLayoutMode) &&
                Objects.equals(elementIds, that.elementIds) &&
                Objects.equals(generatedElementIds, that.generatedElementIds) &&
                Objects.equals(reflowUnitIds, that.reflowUnitIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, operation, sourceText, resultText, requestedLayoutMode,
                requestedLayoutProfile, appliedLayoutMode, elementIds, generatedElementIds, reflowUnitIds);
    }
}
