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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfDancerHttpClientTest {

    @Test
    void prefixesRelativeRequestsWithV2Path() {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));

        client.toBlocking().retrieve(HttpRequest.GET("/pdf/find?types=TEXT_LINE"), String.class);

        assertEquals(URI.create("https://example.test/v2/pdf/find?types=TEXT_LINE"), delegate.lastRequest.uri());
        assertEquals(Optional.of("2"), delegate.lastRequest.headers().firstValue("X-API-VERSION"));
    }

    @Test
    void prefixesSessionAndMutationRequestsWithV2Path() {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));

        client.toBlocking().retrieve(HttpRequest.POST("/session/create", null), String.class);
        assertEquals(URI.create("https://example.test/v2/session/create"), delegate.lastRequest.uri());

        client.toBlocking().retrieve(HttpRequest.PUT("/pdf/move", null), String.class);
        assertEquals(URI.create("https://example.test/v2/pdf/move"), delegate.lastRequest.uri());
    }

    @Test
    void doesNotDoublePrefixAlreadyVersionedPath() {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));

        client.toBlocking().retrieve(HttpRequest.GET("/v2/keys/anon"), String.class);

        assertEquals(URI.create("https://example.test/v2/keys/anon"), delegate.lastRequest.uri());
    }

    private static final class CapturingHttpClient extends HttpClient {
        private java.net.http.HttpRequest lastRequest;

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(java.net.http.HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
            this.lastRequest = request;
            @SuppressWarnings("unchecked")
            T body = (T) "ok".getBytes(StandardCharsets.UTF_8);
            return new StubHttpResponse<>(request, body);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(java.net.http.HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.lastRequest = request;
            @SuppressWarnings("unchecked")
            T body = (T) "ok".getBytes(StandardCharsets.UTF_8);
            return CompletableFuture.completedFuture(new StubHttpResponse<>(request, body));
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                java.net.http.HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return sendAsync(request, responseBodyHandler);
        }
    }

    private static final class StubHttpResponse<T> implements HttpResponse<T> {
        private final java.net.http.HttpRequest request;
        private final T body;

        private StubHttpResponse(java.net.http.HttpRequest request, T body) {
            this.request = request;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public java.net.http.HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public Optional<javax.net.ssl.SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
