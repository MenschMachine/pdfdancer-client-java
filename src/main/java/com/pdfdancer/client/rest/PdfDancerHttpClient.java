package com.pdfdancer.client.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.pdfdancer.client.http.Argument;
import com.pdfdancer.client.http.MediaType;
import com.pdfdancer.client.http.MultipartBody;
import com.pdfdancer.client.http.MutableHttpRequest;
import com.pdfdancer.common.model.ErrorResponse;
import com.pdfdancer.common.model.FontNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;

/**
 * Minimal HTTP client abstraction backed by {@link java.net.http.HttpClient} that mimics
 * the subset of Micronaut's client API used by the original PDFDancer client.
 */
public final class PdfDancerHttpClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient delegate;
    private final URI baseUrl;
    private final ObjectMapper objectMapper;

    private PdfDancerHttpClient(HttpClient delegate, URI baseUrl, ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    public static PdfDancerHttpClient createDefault(URI baseUrl) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
        return new PdfDancerHttpClient(client, baseUrl, createObjectMapper());
    }

    public static PdfDancerHttpClient create(HttpClient httpClient, URI baseUrl) {
        return new PdfDancerHttpClient(httpClient, baseUrl, createObjectMapper());
    }

    public static PdfDancerHttpClient create(HttpClient httpClient, URI baseUrl, ObjectMapper mapper) {
        return new PdfDancerHttpClient(httpClient, baseUrl, mapper == null ? createObjectMapper() : mapper);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public Blocking toBlocking() {
        return new Blocking();
    }

    private <T> T send(MutableHttpRequest<?> request, Class<T> responseType, Argument<T> argument) {
        HttpRequest httpRequest = toJavaRequest(request);
        HttpResponse<byte[]> response;
        try {
            response = delegate.send(httpRequest, BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PdfDancerClientException("HTTP request interrupted", e);
        } catch (IOException e) {
            throw new PdfDancerClientException("HTTP request failed", e);
        }

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw translateError(response);
        }

        byte[] body = response.body();
        if (responseType != null) {
            return decode(body, responseType);
        }

        JavaType javaType = toJavaType(argument);
        try {
            if (body == null || body.length == 0) {
                return null;
            }
            @SuppressWarnings("unchecked")
            T value = (T) readValueFixingTypes(body, javaType);
            return value;
        } catch (IOException e) {
            String preview = new String(body, StandardCharsets.UTF_8);
            throw new PdfDancerClientException("Failed to parse response body: " + preview, e);
        }
    }

    private HttpRequest toJavaRequest(MutableHttpRequest<?> request) {
        URI target = baseUrl.resolve(request.path());
        HttpRequest.Builder builder = HttpRequest.newBuilder(target)
                .timeout(DEFAULT_TIMEOUT);

        request.headers().forEach(builder::header);

        Object body = request.body();
        MediaType declaredContentType = request.contentType();

        if (body == null) {
            builder.method(request.method(), BodyPublishers.noBody());
            return builder.build();
        }

        if (body instanceof byte[]) {
            if (declaredContentType != null) {
                builder.header("Content-Type", declaredContentType.value());
            }
            builder.method(request.method(), BodyPublishers.ofByteArray((byte[]) body));
            return builder.build();
        }

        if (body instanceof MultipartBody) {
            MultipartBody multipart = (MultipartBody) body;
            String boundary = multipart.boundary();
            String contentType = "multipart/form-data; boundary=" + boundary;
            builder.header("Content-Type", contentType);
            builder.method(request.method(), multipartPublisher(multipart));
            return builder.build();
        }

        byte[] json = writeJson(body);
        String contentType = declaredContentType != null
                ? declaredContentType.value()
                : MediaType.APPLICATION_JSON_TYPE.value();
        builder.header("Content-Type", contentType);
        builder.method(request.method(), BodyPublishers.ofByteArray(json));
        return builder.build();
    }

    private java.net.http.HttpRequest.BodyPublisher multipartPublisher(MultipartBody multipart) {
        List<byte[]> byteArrays = new ArrayList<>();
        String boundary = multipart.boundary();
        byte[] separator = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] closing = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        for (MultipartBody.Part part : multipart.parts()) {
            byteArrays.add(separator);

            StringBuilder headers = new StringBuilder("Content-Disposition: form-data; name=\"")
                    .append(part.name())
                    .append("\"");
            if (part.fileName() != null) {
                headers.append("; filename=\"").append(part.fileName()).append("\"");
            }
            headers.append("\r\nContent-Type: ").append(part.contentType().value()).append("\r\n\r\n");

            byteArrays.add(headers.toString().getBytes(StandardCharsets.UTF_8));
            byteArrays.add(part.content());
            byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        byteArrays.add(closing);
        return BodyPublishers.ofByteArrays(byteArrays);
    }

    private RuntimeException translateError(HttpResponse<byte[]> response) {
        int status = response.statusCode();
        byte[] body = response.body();
        Optional<ErrorResponse> error = parseError(body);
        if (status == 404 && error.isPresent()) {
            ErrorResponse err = error.get();
            if ("FontNotFoundException".equals(err.error())) {
                return new FontNotFoundException(err.message());
            }
        }

        String message = error.map(ErrorResponse::message)
                .orElseGet(() -> "Unexpected HTTP status: " + status);
        return new PdfDancerClientException(status, message);
    }

    private Optional<ErrorResponse> parseError(byte[] body) {
        if (body == null || body.length == 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(body, ErrorResponse.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private JavaType toJavaType(Argument<?> argument) {
        if (argument == null) {
            return objectMapper.getTypeFactory().constructType(Object.class);
        }
        Class<?> rawType = argument.rawType();
        Class<?>[] typeArguments = argument.typeArguments();
        if (typeArguments.length == 0) {
            return objectMapper.getTypeFactory().constructType(rawType);
        }
        return objectMapper.getTypeFactory().constructParametricType(rawType, typeArguments);
    }

    private <T> T decode(byte[] body, Class<T> responseType) {
        if (responseType == Void.class || responseType == void.class) {
            return null;
        }
        if (responseType == byte[].class) {
            return (T) body;
        }
        if (responseType == String.class) {
            @SuppressWarnings("unchecked")
            T cast = (T) (body == null ? null : new String(body, StandardCharsets.UTF_8));
            return cast;
        }
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            return responseType.cast(readValueFixingTypes(body, objectMapper.getTypeFactory().constructType(responseType)));
        } catch (IOException e) {
            String preview = new String(body, StandardCharsets.UTF_8);
            throw new PdfDancerClientException("Failed to parse response body: " + preview, e);
        }
    }

    private byte[] writeJson(Object body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            throw new PdfDancerClientException("Failed to serialize request body", e);
        }
    }

    private Object readValueFixingTypes(byte[] body, JavaType javaType) throws IOException {
        JsonNode node = objectMapper.readTree(body);
        ensureObjectRefType(node);
        return objectMapper.readerFor(javaType).readValue(node);
    }

    private void ensureObjectRefType(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            if (!objectNode.has("type") && objectNode.has("objectRefType")) {
                objectNode.set("type", objectNode.get("objectRefType"));
            }
            if (!objectNode.has("objectRefType") && objectNode.has("type")) {
                objectNode.set("objectRefType", objectNode.get("type"));
            }
            objectNode.fields().forEachRemaining(entry -> ensureObjectRefType(entry.getValue()));
        } else if (node.isArray()) {
            node.forEach(this::ensureObjectRefType);
        }
    }

    /**
     * Blocking facade with {@code retrieve} helpers mirroring the Micronaut client.
     */
    public final class Blocking {
        public <T> T retrieve(MutableHttpRequest<?> request, Class<T> responseType) {
            return send(request, responseType, null);
        }

        public <T> T retrieve(MutableHttpRequest<?> request, Argument<T> argument) {
            return send(request, null, argument);
        }
    }
}
