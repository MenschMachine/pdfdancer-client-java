package com.tfc.pdf.pdfdancer.api.common.model;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
public class FormFieldRef extends ObjectRef {
    private final String name;
    private final String value;
    @JsonCreator
    public FormFieldRef(@JsonProperty("internalId") String id,
                        @JsonProperty("position") Position position,
                        @JsonProperty("type") @JsonAlias("objectRefType") ObjectType objectType,
                        @JsonProperty("objectRefType") ObjectType objectRefType,
                        @JsonProperty("name") String name,
                        @JsonProperty("value") String value) {
        super(id, position, objectRefType, objectType);
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
}
