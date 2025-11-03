package com.tfc.pdf.pdfdancer.api.client.rest;

/**
 * Runtime exception used for HTTP or serialization failures in the client.
 */
public class PdfDancerClientException extends RuntimeException {

    private final int statusCode;

    public PdfDancerClientException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public PdfDancerClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public PdfDancerClientException(Throwable cause) {
        super(cause);
        this.statusCode = -1;
    }

    public PdfDancerClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
