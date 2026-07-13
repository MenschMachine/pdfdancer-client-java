package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextEditResponse {
    @JsonProperty("matched")
    private final Integer matched;
    @JsonProperty("changed")
    private final Integer changed;
    @JsonProperty("pagesChanged")
    private final List<Integer> pagesChanged;
    @JsonProperty("change")
    private final List<TextEditChangeDiagnostic> change;
    @JsonProperty("warnings")
    private final List<TextOperationDiagnostic> warnings;
    @JsonProperty("errors")
    private final List<TextOperationDiagnostic> errors;

    @JsonCreator
    public TextEditResponse(@JsonProperty("matched") Integer matched,
                            @JsonProperty("changed") Integer changed,
                            @JsonProperty("pagesChanged") List<Integer> pagesChanged,
                            @JsonProperty("change") List<TextEditChangeDiagnostic> change,
                            @JsonProperty("warnings") List<TextOperationDiagnostic> warnings,
                            @JsonProperty("errors") List<TextOperationDiagnostic> errors) {
        this.matched = matched;
        this.changed = changed;
        this.pagesChanged = pagesChanged == null ? null : List.copyOf(pagesChanged);
        this.change = change == null ? null : List.copyOf(change);
        this.warnings = warnings == null ? null : List.copyOf(warnings);
        this.errors = errors == null ? null : List.copyOf(errors);
    }

    public Integer matched() { return matched; }
    public Integer changed() { return changed; }
    public List<Integer> pagesChanged() { return pagesChanged; }
    public List<TextEditChangeDiagnostic> change() { return change; }
    public List<TextOperationDiagnostic> warnings() { return warnings; }
    public List<TextOperationDiagnostic> errors() { return errors; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextEditResponse) obj;
        return Objects.equals(matched, that.matched) &&
                Objects.equals(changed, that.changed) &&
                Objects.equals(pagesChanged, that.pagesChanged) &&
                Objects.equals(change, that.change) &&
                Objects.equals(warnings, that.warnings) &&
                Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matched, changed, pagesChanged, change, warnings, errors);
    }
}
