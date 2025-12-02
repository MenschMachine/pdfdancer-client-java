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
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;

/**
 * Minimal HTTP client abstraction backed by {@link java.net.http.HttpClient} that mimics
 * the subset of Micronaut's client API used by the original PDFDancer client.
 * <p>
 * By default, clients created without an explicit {@link RetryConfig} will use
 * {@link RetryConfig#defaultConfig()}, which includes retry logic for transient errors
 * (429, 503, etc.) with exponential backoff. To disable retries, explicitly pass
 * {@link RetryConfig#noRetry()} when creating the client.
 * </p>
 */
public final class PdfDancerHttpClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    private static final String DEFAULT_API_VERSION = "1";
    private static final String CLIENT_VERSION = loadClientVersion();

    private static String loadClientVersion() {
        try (InputStream in = PdfDancerHttpClient.class.getResourceAsStream("/pdfdancer-client.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String version = props.getProperty("version", "unknown");
                return "java/" + version;
            }
        } catch (IOException ignored) {
        }
        return "java/unknown";
    }

    private final HttpClient delegate;
    private final URI baseUrl;
    private final ObjectMapper objectMapper;
    private final RetryConfig retryConfig;
    private final String apiVersion;

    private PdfDancerHttpClient(HttpClient delegate, URI baseUrl, ObjectMapper objectMapper, RetryConfig retryConfig, String apiVersion) {
        this.delegate = delegate;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.retryConfig = retryConfig != null ? retryConfig : RetryConfig.defaultConfig();
        this.apiVersion = apiVersion != null ? apiVersion : DEFAULT_API_VERSION;
    }

    public static PdfDancerHttpClient createDefault(URI baseUrl) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
        return new PdfDancerHttpClient(client, baseUrl, createObjectMapper(), null, null);
    }

    public static PdfDancerHttpClient createDefault(URI baseUrl, RetryConfig retryConfig) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
        return new PdfDancerHttpClient(client, baseUrl, createObjectMapper(), retryConfig, null);
    }

    public static PdfDancerHttpClient create(HttpClient httpClient, URI baseUrl) {
        return new PdfDancerHttpClient(httpClient, baseUrl, createObjectMapper(), null, null);
    }

    public static PdfDancerHttpClient create(HttpClient httpClient, URI baseUrl, ObjectMapper mapper) {
        return new PdfDancerHttpClient(httpClient, baseUrl, mapper == null ? createObjectMapper() : mapper, null, null);
    }

    public static PdfDancerHttpClient create(HttpClient httpClient, URI baseUrl, ObjectMapper mapper, RetryConfig retryConfig) {
        return new PdfDancerHttpClient(httpClient, baseUrl, mapper == null ? createObjectMapper() : mapper, retryConfig, null);
    }

    public static PdfDancerHttpClient create(HttpClient httpClient, URI baseUrl, ObjectMapper mapper, RetryConfig retryConfig, String apiVersion) {
        return new PdfDancerHttpClient(httpClient, baseUrl, mapper == null ? createObjectMapper() : mapper, retryConfig, apiVersion);
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

        int maxAttempts = retryConfig.getMaxAttempts();
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpResponse<byte[]> response = delegate.send(httpRequest, BodyHandlers.ofByteArray());

                int status = response.statusCode();
                if (status < 200 || status >= 300) {
                    RuntimeException error = translateError(response);

                    // Check if we should retry based on status code
                    if (attempt < maxAttempts &&
                        error instanceof PdfDancerClientException &&
                        retryConfig.isRetryableStatusCode(status)) {
                        lastException = error;
                        sleep(calculateDelay(attempt, status, response));
                        continue;
                    }

                    throw error;
                }

                // Success - parse and return response
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

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PdfDancerClientException("HTTP request interrupted", e);
            } catch (IOException e) {
                // Check if we should retry on connection error
                if (attempt < maxAttempts && retryConfig.isRetryOnConnectionError()) {
                    lastException = new PdfDancerClientException("HTTP request failed", e);
                    sleep(calculateDelay(attempt, 0, null));
                    continue;
                }
                throw new PdfDancerClientException("HTTP request failed", e);
            } catch (RuntimeException e) {
                // Re-throw runtime exceptions that we already checked for retry
                if (e == lastException) {
                    throw e;
                }
                // For other runtime exceptions, don't retry
                throw e;
            }
        }

        // If we exhausted all retries, throw the last exception
        if (lastException != null) {
            throw lastException;
        }

        throw new PdfDancerClientException("HTTP request failed after " + maxAttempts + " attempts");
    }

    private Duration calculateDelay(int attempt, int statusCode, HttpResponse<byte[]> response) {
        // For 429 responses, check for Retry-After header
        if (statusCode == 429 && response != null) {
            Optional<String> retryAfter = response.headers().firstValue("Retry-After");
            if (retryAfter.isPresent()) {
                Duration retryDelay = parseRetryAfter(retryAfter.get());
                if (retryDelay != null) {
                    // Cap at max delay
                    if (retryDelay.compareTo(retryConfig.getMaxDelay()) > 0) {
                        return retryConfig.getMaxDelay();
                    }
                    return retryDelay;
                }
            }
        }

        // Use exponential backoff for other errors or if Retry-After is missing/invalid
        long delayMillis = (long) (retryConfig.getInitialDelay().toMillis() *
                                   Math.pow(retryConfig.getBackoffMultiplier(), attempt - 1));
        Duration delay = Duration.ofMillis(delayMillis);

        // Cap at max delay
        if (delay.compareTo(retryConfig.getMaxDelay()) > 0) {
            delay = retryConfig.getMaxDelay();
        }

        return delay;
    }

    /**
     * Parses the Retry-After header value.
     * Supports both delay-seconds (integer) and HTTP-date formats.
     *
     * @param retryAfterValue the Retry-After header value
     * @return the parsed delay Duration, or null if parsing fails
     */
    private Duration parseRetryAfter(String retryAfterValue) {
        if (retryAfterValue == null || retryAfterValue.trim().isEmpty()) {
            return null;
        }

        String value = retryAfterValue.trim();

        // Try to parse as delay-seconds (integer)
        try {
            long seconds = Long.parseLong(value);
            if (seconds >= 0) {
                return Duration.ofSeconds(seconds);
            }
        } catch (NumberFormatException e) {
            // Not a number, might be an HTTP-date - fall through
        }

        // Try to parse as HTTP-date
        try {
            java.time.ZonedDateTime futureTime = java.time.ZonedDateTime.parse(
                value,
                java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
            );
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("GMT"));
            long secondsUntil = java.time.Duration.between(now, futureTime).getSeconds();
            if (secondsUntil >= 0) {
                return Duration.ofSeconds(secondsUntil);
            }
        } catch (Exception e) {
            // Failed to parse as HTTP-date
        }

        return null;
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PdfDancerClientException("Retry sleep interrupted", e);
        }
    }

    private HttpRequest toJavaRequest(MutableHttpRequest<?> request) {
        URI target = baseUrl.resolve(request.path());
        HttpRequest.Builder builder = HttpRequest.newBuilder(target)
                .timeout(DEFAULT_TIMEOUT);

        // Add API version header
        builder.header("X-API-VERSION", apiVersion);
        builder.header("X-PDFDancer-Client", CLIENT_VERSION);

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
