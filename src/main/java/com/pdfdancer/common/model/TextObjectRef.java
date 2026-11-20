package com.pdfdancer.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reference to a text line that also carries the text content reported by the server.
 * The text is read-only metadata: it is never sent back to the server when the
 * reference is used in a request.
 */
public class TextObjectRef extends ObjectRef {
    private final String text;

    @JsonCreator
    public TextObjectRef(@JsonProperty("internalId") String internalId,
                         @JsonProperty("position") Position position,
                         @JsonProperty("type") @JsonAlias("objectRefType") ObjectType objectType,
                         @JsonProperty("objectRefType") ObjectType objectRefType,
                         @JsonProperty(value = "text", access = JsonProperty.Access.WRITE_ONLY) String text) {
        super(internalId, position, objectRefType, objectType);
        this.text = text;
    }

    /**
     * Returns the text of this line, or null if the server did not provide it.
     */
    public String getText() {
        return text;
    }
}
