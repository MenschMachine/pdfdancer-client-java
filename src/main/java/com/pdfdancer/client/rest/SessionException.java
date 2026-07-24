package com.pdfdancer.client.rest;

/** Session creation or session-state failure. */
public class SessionException extends PdfDancerException {
    public SessionException(String message) { super(message); }
    public SessionException(String message, Throwable cause) { super(message, cause); }
}
