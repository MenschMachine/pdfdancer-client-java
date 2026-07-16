package com.pdfdancer.client.rest;

import com.pdfdancer.client.http.HttpRequest;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryBehaviorTest {

    @Test
    void maxAttemptsIncludesTheInitialRequest() {
        ScriptedHttpClient delegate = new ScriptedHttpClient(
                Step.status(503), Step.status(503), Step.status(200));
        PdfDancerHttpClient client = client(delegate, RetryConfig.builder()
                .maxAttempts(3).initialDelay(Duration.ZERO).maxDelay(Duration.ZERO).build());

        assertEquals("ok", client.toBlocking().retrieve(HttpRequest.GET("/test"), String.class));
        assertEquals(3, delegate.calls);
    }

    @Test
    void connectionFailureIsRetriedWhenEnabled() {
        ScriptedHttpClient delegate = new ScriptedHttpClient(Step.connectionFailure(), Step.status(200));
        PdfDancerHttpClient client = client(delegate, RetryConfig.builder()
                .maxAttempts(2).initialDelay(Duration.ZERO).maxDelay(Duration.ZERO)
                .retryOnConnectionError(true).build());

        assertEquals("ok", client.toBlocking().retrieve(HttpRequest.GET("/test"), String.class));
        assertEquals(2, delegate.calls);
    }

    @Test
    void finalRateLimitResponsePreservesNumericRetryAfter() {
        RateLimitException error = finalRateLimit("5");
        assertEquals(Duration.ofSeconds(5), error.getRetryAfter());
    }

    @Test
    void finalRateLimitResponseIgnoresMissingInvalidAndNegativeRetryAfter() {
        assertNull(finalRateLimit(null).getRetryAfter());
        assertNull(finalRateLimit("invalid").getRetryAfter());
        assertNull(finalRateLimit("-1").getRetryAfter());
    }

    @Test
    void finalRateLimitResponseParsesHttpDateRetryAfter() {
        String retryAt = ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .plusSeconds(30).format(DateTimeFormatter.RFC_1123_DATE_TIME);

        Duration delay = finalRateLimit(retryAt).getRetryAfter();

        assertTrue(delay != null && !delay.isNegative() && delay.compareTo(Duration.ofSeconds(30)) <= 0);
    }

    private static RateLimitException finalRateLimit(String retryAfter) {
        ScriptedHttpClient delegate = new ScriptedHttpClient(Step.status(429, retryAfter));
        PdfDancerHttpClient client = client(delegate, RetryConfig.noRetry());

        RateLimitException error = assertThrows(RateLimitException.class,
                () -> client.toBlocking().retrieve(HttpRequest.GET("/test"), String.class));
        assertEquals(1, delegate.calls);
        return error;
    }

    private static PdfDancerHttpClient client(HttpClient delegate, RetryConfig retryConfig) {
        return PdfDancerHttpClient.create(delegate, URI.create("https://example.test"), null, retryConfig);
    }

    private record Step(int status, String retryAfter, IOException failure) {
        static Step status(int status) { return new Step(status, null, null); }
        static Step status(int status, String retryAfter) { return new Step(status, retryAfter, null); }
        static Step connectionFailure() { return new Step(0, null, new IOException("connection failed")); }
    }

    private static final class ScriptedHttpClient extends HttpClient {
        private final Queue<Step> steps = new ArrayDeque<>();
        private int calls;

        private ScriptedHttpClient(Step... steps) {
            this.steps.addAll(java.util.List.of(steps));
        }

        @Override
        public <T> HttpResponse<T> send(java.net.http.HttpRequest request,
                                        HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            calls++;
            Step step = steps.remove();
            if (step.failure() != null) throw step.failure();
            @SuppressWarnings("unchecked")
            T body = (T) (step.status() == 200 ? "ok" : "{\"message\":\"limited\"}")
                    .getBytes(StandardCharsets.UTF_8);
            return new StubResponse<>(request, step.status(), step.retryAfter(), body);
        }

        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public SSLParameters sslParameters() { return null; }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                java.net.http.HttpRequest request, HttpResponse.BodyHandler<T> handler) {
            throw new UnsupportedOperationException();
        }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                java.net.http.HttpRequest request, HttpResponse.BodyHandler<T> handler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }
    }

    private record StubResponse<T>(java.net.http.HttpRequest request, int statusCode,
                                   String retryAfter, T body) implements HttpResponse<T> {
        @Override public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() {
            Map<String, java.util.List<String>> values = retryAfter == null
                    ? Map.of() : Map.of("Retry-After", java.util.List.of(retryAfter));
            return HttpHeaders.of(values, (name, value) -> true);
        }
        @Override public Optional<javax.net.ssl.SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return request.uri(); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }
}
