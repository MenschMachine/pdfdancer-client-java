package com.pdfdancer.client.rest;

/** HTTP transport or non-success response failure. */
public class HttpClientException extends PdfDancerException {
    private final int statusCode;

    public HttpClientException(String message) { this(-1, message, null); }
    public HttpClientException(String message, Throwable cause) { this(-1, message, cause); }
    public HttpClientException(Throwable cause) { this(-1, cause.getMessage(), cause); }
    public HttpClientException(int statusCode, String message) { this(statusCode, message, null); }
    public HttpClientException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    public int getStatusCode() { return statusCode; }
}
