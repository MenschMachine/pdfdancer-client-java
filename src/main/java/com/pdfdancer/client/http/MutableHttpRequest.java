package com.pdfdancer.client.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simplified mutable HTTP request used by the PDFDancer client.
 * Captures method, path, headers, and body without depending on Micronaut classes.
 *
 * @param <T> body type
 */
public final class MutableHttpRequest<T> {

    private final String method;
    private final String path;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private T body;
    private MediaType contentType;

    MutableHttpRequest(String method, String path, T body) {
        this.method = Objects.requireNonNull(method, "method");
        this.path = Objects.requireNonNull(path, "path");
        this.body = body;
    }

    public MutableHttpRequest<T> contentType(MediaType mediaType) {
        this.contentType = mediaType;
        return this;
    }

    public MutableHttpRequest<T> bearerAuth(String token) {
        if (token != null && !token.isBlank()) {
            header("Authorization", "Bearer " + token);
        }
        return this;
    }

    public MutableHttpRequest<T> header(String name, String value) {
        if (name != null && value != null) {
            headers.put(name, value);
        }
        return this;
    }

    public MutableHttpRequest<T> body(T newBody) {
        this.body = newBody;
        return this;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public T body() {
        return body;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public MediaType contentType() {
        return contentType;
    }
}
