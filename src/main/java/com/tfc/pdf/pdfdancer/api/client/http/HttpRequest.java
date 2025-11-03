package com.tfc.pdf.pdfdancer.api.client.http;

/**
 * Factory for simplified HTTP requests used by the PDFDancer client.
 */
public final class HttpRequest {

    private HttpRequest() {
    }

    public static <T> MutableHttpRequest<T> GET(String path) {
        return new MutableHttpRequest<>("GET", path, null);
    }

    public static <T> MutableHttpRequest<T> POST(String path, T body) {
        return new MutableHttpRequest<>("POST", path, body);
    }

    public static <T> MutableHttpRequest<T> PUT(String path, T body) {
        return new MutableHttpRequest<>("PUT", path, body);
    }

    public static <T> MutableHttpRequest<T> DELETE(String path, T body) {
        return new MutableHttpRequest<>("DELETE", path, body);
    }
}
