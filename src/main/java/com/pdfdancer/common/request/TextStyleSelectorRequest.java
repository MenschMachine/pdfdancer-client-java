package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextStyleSelectorRequest {
    @JsonProperty("literal")
    private final String literal;
    @JsonProperty("regex")
    private final String regex;
    @JsonProperty("caseSensitive")
    private final Boolean caseSensitive;
    @JsonProperty("wholeWords")
    private final Boolean wholeWords;
    @JsonProperty("maxMatches")
    private final Integer maxMatches;
    @JsonProperty("runs")
    private final TextStyleRunsSelectorRequest runs;

    @JsonCreator
    public TextStyleSelectorRequest(@JsonProperty("literal") String literal,
                                    @JsonProperty("regex") String regex,
                                    @JsonProperty("caseSensitive") Boolean caseSensitive,
                                    @JsonProperty("wholeWords") Boolean wholeWords,
                                    @JsonProperty("maxMatches") Integer maxMatches,
                                    @JsonProperty("runs") TextStyleRunsSelectorRequest runs) {
        this.literal = literal;
        this.regex = regex;
        this.caseSensitive = caseSensitive;
        this.wholeWords = wholeWords;
        this.maxMatches = maxMatches;
        this.runs = runs;
    }

    static TextStyleSelectorRequest from(TextSelectorRequest selector) {
        if (selector == null) {
            return null;
        }
        return new TextStyleSelectorRequest(
                selector.literal(),
                selector.regex(),
                selector.caseSensitive(),
                selector.wholeWords(),
                selector.maxMatches(),
                null);
    }

    public String literal() { return literal; }
    public String regex() { return regex; }
    public Boolean caseSensitive() { return caseSensitive; }
    public Boolean wholeWords() { return wholeWords; }
    public Integer maxMatches() { return maxMatches; }
    public TextStyleRunsSelectorRequest runs() { return runs; }

    public TextStyleSelectorRequest validated() {
        boolean hasLiteral = literal != null;
        boolean hasRegex = regex != null;
        boolean hasRuns = runs != null;
        int branches = (hasLiteral ? 1 : 0) + (hasRegex ? 1 : 0) + (hasRuns ? 1 : 0);
        if (branches != 1) {
            throw new IllegalArgumentException("Exactly one of literal, regex, or runs must be provided");
        }
        if (hasLiteral && literal.isBlank()) {
            throw new IllegalArgumentException("literal must not be blank");
        }
        if (hasRegex && regex.isBlank()) {
            throw new IllegalArgumentException("regex must not be blank");
        }
        if (maxMatches != null && maxMatches <= 0) {
            throw new IllegalArgumentException("maxMatches must be positive");
        }
        if (runs != null) {
            runs.validated();
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextStyleSelectorRequest) obj;
        return Objects.equals(literal, that.literal) &&
                Objects.equals(regex, that.regex) &&
                Objects.equals(caseSensitive, that.caseSensitive) &&
                Objects.equals(wholeWords, that.wholeWords) &&
                Objects.equals(maxMatches, that.maxMatches) &&
                Objects.equals(runs, that.runs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literal, regex, caseSensitive, wholeWords, maxMatches, runs);
    }

    @Override
    public String toString() {
        return "TextStyleSelectorRequest[" +
                "literal=" + literal + ", " +
                "regex=" + regex + ", " +
                "caseSensitive=" + caseSensitive + ", " +
                "wholeWords=" + wholeWords + ", " +
                "maxMatches=" + maxMatches + ", " +
                "runs=" + runs + ']';
    }
}
