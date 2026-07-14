package com.pdfdancer.client.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfdancer.common.request.TextDeleteRequest;
import com.pdfdancer.common.request.TextInsertRequest;
import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PdfAffineTransform;
import com.pdfdancer.common.response.TextEditResponse;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextReplaceApiTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void documentReplacePostsToTextReplaceEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        TextEditResponse response = pdf.text().replace(TextReplaceRequest.literal("Acme", "Globex").build());

        assertEquals(2, response.matched());
        assertEquals(1, response.changed());
        assertEquals(URI.create("https://example.test/v2/pdf/text/replace"), delegate.lastRequest.uri());
        assertEquals(Optional.of("token"), delegate.lastRequest.headers().firstValue("Authorization").map(v -> v.replace("Bearer ", "")));
        assertEquals(Optional.of("session-123"), delegate.lastRequest.headers().firstValue("X-Session-Id"));

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("Acme", json.at("/select/literal").asText());
        assertEquals("Globex", json.at("/replaceWith").asText());
    }

    @Test
    void pageReplaceSetsSinglePageScope() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.page(2).text().replace(TextReplaceRequest.literal("Acme", "Globex").pages(9).build());

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(2, json.at("/pages/0").asInt());
        assertEquals(1, json.at("/pages").size());
    }

    @Test
    void documentReplacePostsAtomicStyleOverrides() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.text().replace(TextReplaceRequest.literal("Acme", "Globex")
                .font("Helvetica-Bold")
                .size(17)
                .fillColor(PdfColorRequest.rgb(0.1, 0.2, 0.3))
                .strokeColor(PdfColorRequest.gray(0.4))
                .characterSpacing(0.25)
                .wordSpacing(1.5)
                .build());

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("Helvetica-Bold", json.at("/style/font").asText());
        assertEquals(17.0, json.at("/style/size").asDouble());
        assertEquals("rgb", json.at("/style/fillColor/space").asText());
        assertEquals("gray", json.at("/style/strokeColor/space").asText());
        assertEquals(0.25, json.at("/style/characterSpacing").asDouble());
        assertEquals(1.5, json.at("/style/wordSpacing").asDouble());
    }

    @Test
    void documentImageReplacePostsCaretRelativePdfMatrix() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);
        Image image = new Image();
        image.setData(new byte[]{1, 2, 3});

        TextEditResponse response = pdf.text().replace(TextReplaceRequest.builder()
                .literal("{{logo}}")
                .replaceWithImage(image, PdfAffineTransform.fromPdfMatrix(
                        new double[]{20, 0, 5, 10, 3, -2}))
                .build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/replace"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("AQID", json.at("/replaceWithImage/data").asText());
        assertEquals(20.0, json.at("/replaceWithImage/transformationMatrix/0").asDouble());
        assertEquals(5.0, json.at("/replaceWithImage/transformationMatrix/2").asDouble());
        assertEquals(-2.0, json.at("/replaceWithImage/transformationMatrix/5").asDouble());
        assertEquals(true, json.at("/replaceWith").isMissingNode());
        assertEquals("img_456", response.change().get(0).generatedElementIds().get(0));
    }

    @Test
    void pageImageReplaceOverridesRequestPageScope() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);
        Image image = new Image();
        image.setData(new byte[]{1});

        pdf.page(2).text().replace(TextReplaceRequest.builder()
                .literal("{{logo}}")
                .pages(9)
                .replaceWithImage(image, PdfAffineTransform.builder().translate(3, -2).build())
                .build());

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(2, json.at("/pages/0").asInt());
        assertEquals(1, json.at("/pages").size());
        assertEquals(1.0, json.at("/replaceWithImage/transformationMatrix/0").asDouble());
        assertEquals(3.0, json.at("/replaceWithImage/transformationMatrix/4").asDouble());
    }

    @Test
    void documentDeletePostsToTextDeleteEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        TextEditResponse response = pdf.text().delete(TextDeleteRequest.literal("Acme").build());

        assertEquals(2, response.matched());
        assertEquals(1, response.changed());
        assertEquals(URI.create("https://example.test/v2/pdf/text/delete"), delegate.lastRequest.uri());
        assertEquals(Optional.of("token"), delegate.lastRequest.headers().firstValue("Authorization").map(v -> v.replace("Bearer ", "")));
        assertEquals(Optional.of("session-123"), delegate.lastRequest.headers().firstValue("X-Session-Id"));

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("Acme", json.at("/select/literal").asText());
        assertEquals(true, json.at("/replaceWith").isMissingNode());
    }

    @Test
    void pageDeleteSetsSinglePageScope() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.page(2).text().delete(TextDeleteRequest.literal("Acme").pages(9).build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/delete"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(2, json.at("/pages/0").asInt());
        assertEquals(1, json.at("/pages").size());
    }

    @Test
    void documentInsertPostsToTextInsertEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        TextEditResponse response = pdf.text().insert(TextInsertRequest.after("Acme", " Corp").build());

        assertEquals(2, response.matched());
        assertEquals(1, response.changed());
        assertEquals(URI.create("https://example.test/v2/pdf/text/insert"), delegate.lastRequest.uri());
        assertEquals(Optional.of("token"), delegate.lastRequest.headers().firstValue("Authorization").map(v -> v.replace("Bearer ", "")));
        assertEquals(Optional.of("session-123"), delegate.lastRequest.headers().firstValue("X-Session-Id"));

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("Acme", json.at("/target/anchor/select/literal").asText());
        assertEquals("after", json.at("/target/anchor/caret").asText());
        assertEquals(" Corp", json.at("/insert").asText());
        assertEquals("anchor", json.at("/style/from").asText());
        assertEquals(true, json.at("/style/patch").isMissingNode());
    }

    @Test
    void pageInsertSetsSingleAnchorPageScope() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.page(2).text().insert(TextInsertRequest.before("Acme", "The ").pages(9).build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/insert"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(2, json.at("/target/anchor/pages/0").asInt());
        assertEquals(1, json.at("/target/anchor/pages").size());
        assertEquals("before", json.at("/target/anchor/caret").asText());
    }

    @Test
    void documentInsertPostsStylePatchToTextInsertEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.text().insert(TextInsertRequest.after("Acme", " Corp")
                .size(12)
                .fillColor(PdfColorRequest.rgb(1, 0, 0))
                .build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/insert"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("anchor", json.at("/style/from").asText());
        assertEquals(12.0, json.at("/style/patch/size").asDouble());
        assertEquals("rgb", json.at("/style/patch/fillColor/space").asText());
        assertEquals(1.0, json.at("/style/patch/fillColor/components/0").asDouble());
    }

    @Test
    void documentInsertPostsCoordinateTargetToTextInsertEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.text().insert(TextInsertRequest.at(1, 72, 144, "Coordinate Text")
                .rotationDegrees(90)
                .font("Helvetica")
                .size(12)
                .fillColor(PdfColorRequest.rgb(1, 0, 0))
                .build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/insert"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(1, json.at("/target/coordinate/page").asInt());
        assertEquals(72.0, json.at("/target/coordinate/x").asDouble());
        assertEquals(144.0, json.at("/target/coordinate/y").asDouble());
        assertEquals(90.0, json.at("/target/coordinate/rotationDegrees").asDouble());
        assertEquals(true, json.at("/target/anchor").isMissingNode());
        assertEquals(true, json.at("/style/from").isMissingNode());
        assertEquals("Helvetica", json.at("/style/patch/font").asText());
        assertEquals(12.0, json.at("/style/patch/size").asDouble());
    }

    @Test
    void pageInsertSetsSingleCoordinatePageScope() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.page(2).text().insert(TextInsertRequest.builder()
                .coordinate(72, 144)
                .insert("Page coordinate")
                .font("Helvetica")
                .size(12)
                .build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/insert"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(2, json.at("/target/coordinate/page").asInt());
        assertEquals(72.0, json.at("/target/coordinate/x").asDouble());
        assertEquals(144.0, json.at("/target/coordinate/y").asDouble());
    }

    @Test
    void documentStylePostsToTextStyleEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        TextEditResponse response = pdf.text().style(TextStyleRequest.literal("Acme")
                .fillColor(PdfColorRequest.rgb(1, 0, 0))
                .size(12)
                .build());

        assertEquals(2, response.matched());
        assertEquals(1, response.changed());
        assertEquals(URI.create("https://example.test/v2/pdf/text/style"), delegate.lastRequest.uri());
        assertEquals(Optional.of("token"), delegate.lastRequest.headers().firstValue("Authorization").map(v -> v.replace("Bearer ", "")));
        assertEquals(Optional.of("session-123"), delegate.lastRequest.headers().firstValue("X-Session-Id"));

        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("Acme", json.at("/select/literal").asText());
        assertEquals(12.0, json.at("/style/size").asDouble());
        assertEquals("rgb", json.at("/style/fillColor/space").asText());
        assertEquals(1.0, json.at("/style/fillColor/components/0").asDouble());
    }

    @Test
    void pageStyleSetsSinglePageScope() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.page(2).text().style(TextStyleRequest.literal("Acme")
                .pages(9)
                .fillColor(PdfColorRequest.gray(0.5))
                .build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/style"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals(2, json.at("/pages/0").asInt());
        assertEquals(1, json.at("/pages").size());
        assertEquals("gray", json.at("/style/fillColor/space").asText());
    }

    @Test
    void documentStylePostsRunsWhereSelectorToTextStyleEndpoint() throws Exception {
        CapturingHttpClient delegate = new CapturingHttpClient();
        PdfDancerHttpClient client = PdfDancerHttpClient.create(delegate, URI.create("https://example.test"));
        PDFDancer pdf = PDFDancer.createSession("token", new byte[]{1, 2, 3}, client);

        pdf.text().style(TextStyleRequest.runsWhere()
                .whereTextContains("Acme")
                .whereSize(12, 0.01)
                .maxMatches(10)
                .fillColor(PdfColorRequest.rgb(1, 0, 0))
                .build());

        assertEquals(URI.create("https://example.test/v2/pdf/text/style"), delegate.lastRequest.uri());
        JsonNode json = mapper.readTree(delegate.lastBody);
        assertEquals("Acme", json.at("/select/runs/where/textContains").asText());
        assertEquals(12.0, json.at("/select/runs/where/size/eq").asDouble());
        assertEquals(0.01, json.at("/select/runs/where/size/tolerance").asDouble());
        assertEquals(10, json.at("/select/runs/maxMatches").asInt());
        assertEquals(true, json.at("/select/runs/where/runIds").isMissingNode());
        assertEquals(true, json.at("/select/runs/where/reflowUnitIds").isMissingNode());
        assertEquals(true, json.at("/select/runs/where/elementIdsAny").isMissingNode());
        assertEquals("rgb", json.at("/style/fillColor/space").asText());
    }

    private static final class CapturingHttpClient extends HttpClient {
        private java.net.http.HttpRequest lastRequest;
        private String lastBody;

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
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(java.net.http.HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            this.lastRequest = request;
            this.lastBody = readBody(request);
            @SuppressWarnings("unchecked")
            T body = (T) responseBodyFor(request).getBytes(StandardCharsets.UTF_8);
            return new StubHttpResponse<>(request, body);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(java.net.http.HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            try {
                return CompletableFuture.completedFuture(send(request, responseBodyHandler));
            } catch (IOException e) {
                CompletableFuture<HttpResponse<T>> failed = new CompletableFuture<>();
                failed.completeExceptionally(e);
                return failed;
            }
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                java.net.http.HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return sendAsync(request, responseBodyHandler);
        }

        private static String responseBodyFor(java.net.http.HttpRequest request) {
            if (request.uri().getPath().endsWith("/session/create")) {
                return "session-123";
            }
            return """
                    {
                      "matched": 2,
                      "changed": 1,
                      "pagesChanged": [1],
                      "change": [
                        {
                          "page": 1,
                          "operation": "replace",
                          "sourceText": "Acme",
                          "resultText": "Globex",
                          "requestedLayoutMode": "sourceAnchored",
                          "appliedLayoutMode": "SOURCE_ANCHORED",
                          "elementIds": ["txt_123"],
                          "generatedElementIds": ["img_456"]
                        }
                      ],
                      "warnings": [],
                      "errors": []
                    }
                    """;
        }

        private static String readBody(java.net.http.HttpRequest request) throws IOException {
            Optional<java.net.http.HttpRequest.BodyPublisher> publisher = request.bodyPublisher();
            if (publisher.isEmpty()) {
                return "";
            }
            CompletableFuture<byte[]> body = new CompletableFuture<>();
            publisher.get().subscribe(new Flow.Subscriber<>() {
                private final ByteArrayOutputStream out = new ByteArrayOutputStream();
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    byte[] bytes = new byte[item.remaining()];
                    item.get(bytes);
                    out.writeBytes(bytes);
                }

                @Override
                public void onError(Throwable throwable) {
                    body.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    body.complete(out.toByteArray());
                }
            });
            try {
                return new String(body.get(5, TimeUnit.SECONDS), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IOException("Failed to read request body", e);
            }
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
