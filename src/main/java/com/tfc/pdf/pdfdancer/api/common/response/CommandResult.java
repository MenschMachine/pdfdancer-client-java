package com.tfc.pdf.pdfdancer.api.common.response;
public record CommandResult(String commandName, String elementId, String message, boolean success, String warning) {
}
