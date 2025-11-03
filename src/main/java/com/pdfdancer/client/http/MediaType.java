package com.pdfdancer.client.http;

/**
 * Minimal representation of an HTTP media type used by the PDFDancer client.
 * Provides constants for the content types used by the REST API.
 */
public final class MediaType {

    public static final MediaType APPLICATION_JSON_TYPE = new MediaType("application/json");
    public static final MediaType APPLICATION_PDF_TYPE = new MediaType("application/pdf");
    public static final MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart/form-data");
    public static final MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application/octet-stream");
    public static final MediaType TEXT_PLAIN_TYPE = new MediaType("text/plain");

    private final String value;

    public MediaType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
