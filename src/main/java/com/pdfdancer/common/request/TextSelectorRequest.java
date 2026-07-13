package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextSelectorRequest {
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

    @JsonCreator
    public TextSelectorRequest(@JsonProperty("literal") String literal,
                               @JsonProperty("regex") String regex,
                               @JsonProperty("caseSensitive") Boolean caseSensitive,
                               @JsonProperty("wholeWords") Boolean wholeWords,
                               @JsonProperty("maxMatches") Integer maxMatches) {
        this.literal = literal;
        this.regex = regex;
        this.caseSensitive = caseSensitive;
        this.wholeWords = wholeWords;
        this.maxMatches = maxMatches;
    }

    public String literal() { return literal; }
    public String regex() { return regex; }
    public Boolean caseSensitive() { return caseSensitive; }
    public Boolean wholeWords() { return wholeWords; }
    public Integer maxMatches() { return maxMatches; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextSelectorRequest) obj;
        return Objects.equals(literal, that.literal) &&
                Objects.equals(regex, that.regex) &&
                Objects.equals(caseSensitive, that.caseSensitive) &&
                Objects.equals(wholeWords, that.wholeWords) &&
                Objects.equals(maxMatches, that.maxMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literal, regex, caseSensitive, wholeWords, maxMatches);
    }

    @Override
    public String toString() {
        return "TextSelectorRequest[" +
                "literal=" + literal + ", " +
                "regex=" + regex + ", " +
                "caseSensitive=" + caseSensitive + ", " +
                "wholeWords=" + wholeWords + ", " +
                "maxMatches=" + maxMatches + ']';
    }
}
