package com.pdfdancer.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class CommandResult {
    @JsonProperty("commandName")
    private final String commandName;
    @JsonProperty("elementId")
    private final String elementId;
    @JsonProperty("message")
    private final String message;
    @JsonProperty("success")
    private final boolean success;
    @JsonProperty("warning")
    private final String warning;

    @JsonCreator
    public CommandResult(@JsonProperty("commandName") String commandName,
                         @JsonProperty("elementId") String elementId,
                         @JsonProperty("message") String message,
                         @JsonProperty("success") boolean success,
                         @JsonProperty("warning") String warning) {
        this.commandName = commandName;
        this.elementId = elementId;
        this.message = message;
        this.success = success;
        this.warning = warning;
    }

    public String commandName() {
        return commandName;
    }

    public String elementId() {
        return elementId;
    }

    public String message() {
        return message;
    }

    public boolean success() {
        return success;
    }

    public String warning() {
        return warning;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CommandResult) obj;
        return Objects.equals(this.commandName, that.commandName) &&
                Objects.equals(this.elementId, that.elementId) &&
                Objects.equals(this.message, that.message) &&
                this.success == that.success &&
                Objects.equals(this.warning, that.warning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandName, elementId, message, success, warning);
    }

    @Override
    public String toString() {
        return "CommandResult[" +
                "commandName=" + commandName + ", " +
                "elementId=" + elementId + ", " +
                "message=" + message + ", " +
                "success=" + success + ", " +
                "warning=" + warning + ']';
    }

}
