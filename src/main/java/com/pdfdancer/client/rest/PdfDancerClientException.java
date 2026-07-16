package com.pdfdancer.client.rest;

/**
 * Runtime exception used for HTTP or serialization failures in the client.
 */
public class PdfDancerClientException extends HttpClientException {

    public PdfDancerClientException(String message) {
        super(message);
    }

    public PdfDancerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public PdfDancerClientException(Throwable cause) {
        super(cause);
    }

    public PdfDancerClientException(int statusCode, String message) {
        super(statusCode, message);
    }
}
